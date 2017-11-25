package io.proximi.proximiiodemo;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Created by ihb on 25.11.17.
 */

public interface MyService {

    @POST("/")
    Call<ResponseBody> enter(@Body RequestBody body);

    @PUT("/")
    Call<ResponseBody> exit(@Body RequestBody body);
}
