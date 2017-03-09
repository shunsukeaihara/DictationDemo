package jp.argmax.dictationdemo.recorder;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by aihara on 2017/03/09.
 */

class UtteranceChunker {

    private static final int UTTETRANCE_STATUS_PREBUFFERING = 0;
    private static final int UTTETRANCE_STATUS_SPEECH = 1;
    static final int UTTETRANCE_STATUS_FINISSHED = 2;
    @IntDef({UTTETRANCE_STATUS_PREBUFFERING, UTTETRANCE_STATUS_SPEECH, UTTETRANCE_STATUS_FINISSHED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface ChunkStatus {}


    private static final int FRONT_BUFFER_LENGTH = 20;
    private static final int NO_SPEECH_COUNT_THRESHOLD = 30;


    private ArrayDeque<byte[]> frontBuffer;
    private ArrayList<byte[]> utteranceList;
    private final int windowsize;
    private @ChunkStatus int status;
    private int noSpeechCount;


    UtteranceChunker(int winsize){
        frontBuffer = new ArrayDeque<>(FRONT_BUFFER_LENGTH);
        utteranceList = new ArrayList<>();
        windowsize = winsize * 2;
        noSpeechCount = 0;
        status = UTTETRANCE_STATUS_PREBUFFERING;
    }

    int Add(byte[] window, boolean isSpeech){
        switch (status) {
            case UTTETRANCE_STATUS_PREBUFFERING:
                if (isSpeech) {
                    // 発話なら状態遷移
                    status = UTTETRANCE_STATUS_SPEECH;
                    utteranceList.add(window);
                } else {
                    if (frontBuffer.size() == FRONT_BUFFER_LENGTH) {
                        //溢れそうなら削除
                        frontBuffer.poll();
                    }
                    frontBuffer.add(window);
                }
                break;
            case UTTETRANCE_STATUS_SPEECH:
                if (isSpeech) {
                    noSpeechCount = 0;
                    utteranceList.add(window);
                } else {
                    noSpeechCount++;
                    if (noSpeechCount > NO_SPEECH_COUNT_THRESHOLD) {
                        status = UTTETRANCE_STATUS_FINISSHED;
                    } else {
                        utteranceList.add(window);
                    }
                }
                break;
            case UTTETRANCE_STATUS_FINISSHED:
                break;
        }

        return status;
    }

    byte[] toByteArray(){
        int length = (frontBuffer.size() + utteranceList.size()) * windowsize;
        byte[] utterance = new byte[length];
        int pos = 0;
        Iterator<byte[]> it = frontBuffer.iterator();
        while(it.hasNext()){
            System.arraycopy(it.next(),0, utterance, pos * windowsize, windowsize);
            pos++;
        }
        it = utteranceList.iterator();
        while(it.hasNext()){
            System.arraycopy(it.next(),0, utterance, pos * windowsize, windowsize);
            pos++;
        }
        frontBuffer.clear();
        utteranceList.clear();
        return utterance;
    }
}
