package jp.argmax.dictationdemo.recorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.argmax.dictationdemo.R;
import jp.argmax.dictationdemo.ndev.NdevAsrInterface;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class MicRecordingASRService extends Service {

    private RecorderOnSubscrib recorder;
    private final Context context = this;
    private final Handler go = new Handler();
    private Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();
        recorder = new RecorderOnSubscrib();
        retrofit = new Retrofit.Builder()
                .baseUrl(NdevAsrInterface.END_POINT).build();

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

        // 別クラスに切り出し？
        RequestBody rb = RequestBody.create(MediaType.parse("audio/x-wav;codec=pcm;bit=16;rate=16000"), utterance);
        NdevAsrInterface nai = retrofit.create(NdevAsrInterface.class);
        Call<ResponseBody> call = nai.requestASR(getString(R.string.NDEV_APP_ID), getString(R.string.NDEV_APP_KEY), "jeijfiuhe8we98", rb);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                if(response.code() == 200) {
                    String st;
                    try {
                        st = response.body().string();
                    } catch (Exception e) {
                        Log.d(this.getClass().toString(), "asr request error");
                        return;
                    }
                    Intent i = new Intent();
                    i.putExtra("ASR_RESULT", st);
                    i.setAction("SEND_ASR_RESULT");
                    getBaseContext().sendBroadcast(i);
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(this.getClass().toString(), "http error");
            }
        });

    }

    private void asrError(Throwable e){

    }

}
