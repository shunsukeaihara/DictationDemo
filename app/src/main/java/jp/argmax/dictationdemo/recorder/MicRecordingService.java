package jp.argmax.dictationdemo.recorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MicRecordingService extends Service {

    private RecorderOnSubscrib recorder;
    private final Context context = this;
    private final Handler go = new Handler();


    @Override
    public void onCreate() {
        super.onCreate();
        recorder = new RecorderOnSubscrib();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //別スレッドで録音開始
        Observable.create(recorder)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::asr,
                        this::asrError
                );

        return START_REDELIVER_INTENT;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recorder.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void asr(byte[] utterance) {
        go.post(() -> Toast.makeText(context, "utterance", Toast.LENGTH_LONG).show());
    }

    private void asrError(Throwable e){

    }

}
