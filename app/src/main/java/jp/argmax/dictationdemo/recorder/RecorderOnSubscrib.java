package jp.argmax.dictationdemo.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by aihara on 2017/03/09.
 */

public class RecorderOnSubscrib implements ObservableOnSubscribe<byte[]> {

    private ObservableEmitter<byte[]> utteranceEmitter;
    private boolean isRecording = false;
    private AudioRecord recorder;
    private static final int windowsize = 1024; //shortでのサンプル数

    private static final double dbThreshold = -30.0;
    private static final int zcThreshold = 100;

    private final Object mLock = new Object();

    @Override
    public void subscribe(ObservableEmitter<byte[]> e) throws Exception {
        utteranceEmitter = e;
        if(!isRecording) {
            isRecording = true;
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, windowsize * 2 * 3);
            recorder.startRecording();
            run();
        }
    }

    public void stop() {
        synchronized (mLock) {
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop();
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                recorder.release();
            }
            utteranceEmitter.onComplete();
        }
    }

    private void run() {

        short[] buf = new short[windowsize];

        UtteranceChunker ut = new UtteranceChunker(windowsize);
        while (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
            recorder.read(buf, 0, buf.length);
            ByteBuffer nativeOriginBuffer = ByteBuffer.allocateDirect(windowsize * 2).order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer nativeBuffer = nativeOriginBuffer.asShortBuffer();
            nativeBuffer.position(0);
            nativeBuffer.put(buf, 0, buf.length).position(0);
            nativeOriginBuffer.position(0);
            byte[] ba = nativeOriginBuffer.array();

            // 発話判定
            boolean isSpeech = isSpeech(buf);

            int utteranceStatus = ut.Add(ba, isSpeech);
            if (utteranceStatus == UtteranceChunker.UTTETRANCE_STATUS_FINISSHED) {
                // 発話チャンクをemmit
                utteranceEmitter.onNext(ut.toByteArray());
                ut = new UtteranceChunker(windowsize);
                ut.Add(ba, isSpeech);
            }
        }
        mLock.notifyAll();
    }

    private boolean isSpeech(short[] window){
        // 要検討
        double db = calcPower(window);
        int zc = calcZeroCross(window);
        return zc > zcThreshold && db > dbThreshold;
    }

    private double calcPower(short[] window){
        double sum = 0.0;
        for (short aWindow : window) {
            sum += Math.pow((double) aWindow / 32768.0, 2.0);
        }
        double p = Math.sqrt(sum / window.length);

        return 20.0 * Math.log10(p);
    }

    private int calcZeroCross(short[] window) {
        int count = 0;
        for(int i = 1; i< window.length; i++) {
            if(window[i-1] * window[i] < 0){
                count++;
            }
        }
        return count;
    }
}
