package jp.argmax.dictationdemo.ndev;

import jp.argmax.dictationdemo.R;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by aihara on 2017/03/10.
 */

public class ASRClient {

    private String mAppId;
    private String mAppKey;
    private NdevAsrInterface nai;
    private String mSpeakerId;


    public ASRClient(String appId, String appKey){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NdevAsrInterface.END_POINT).build();
        nai = retrofit.create(NdevAsrInterface.class);
        mAppId = appId;
        mAppKey = appKey;
        mSpeakerId = "sadkaoskdoska";
    }

    public void Call(byte[] utterance, Callback<ResponseBody> cb){
        RequestBody rb = RequestBody.create(MediaType.parse("audio/x-wav;codec=pcm;bit=16;rate=16000"), utterance);
        Call<ResponseBody> call = nai.requestASR(mAppId, mAppKey, mSpeakerId, rb);
        call.enqueue(cb);
        return;
    }
}
