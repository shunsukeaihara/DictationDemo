package jp.argmax.dictationdemo.ndev;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by aihara on 2017/03/07.
 */

public interface NdevAsrInterface {

    String END_POINT = "https://dictation.nuancemobility.net/NMDPAsrCmdServlet/";

    @Headers({
            "Accept: text/plain",
            "Content-type: audio/x-wav;codec=pcm;bit=16;rate=16000",
            "Accept-Language: jpn-JPN",
            "Accept-Topic: Dictation"
    })

    @POST("dictation")
    Call<ResponseBody> requestASR(@Query("appId") String appId, @Query("appKey") String appkey,
                                  @Query("id") String reqid,
                                  @Body RequestBody pcm);



    // byte[] a = new byte[10];
    //RequestBody.create(MediaType.parse("aaa"), a);
}
