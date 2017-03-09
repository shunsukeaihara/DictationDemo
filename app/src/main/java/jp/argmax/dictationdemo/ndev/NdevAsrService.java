package jp.argmax.dictationdemo.ndev;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by aihara on 2017/03/07.
 */

public interface NdevAsrService {
    @Headers({
            "Accept: text/plain",
            "Content-type: audio/x-wav;codec=pcm;bit=16;rate=16000",
            "Accept-Language: ja-JP",
            "Accept-Topic: Dictation"
    })

    @POST("users/{user}/repos")
    Observable<String> requestASR(@Body RequestBody pcm);



    // byte[] a = new byte[10];
    //RequestBody.create(MediaType.parse("aaa"), a);
}
