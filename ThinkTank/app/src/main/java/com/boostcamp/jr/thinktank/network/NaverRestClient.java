package com.boostcamp.jr.thinktank.network;

import com.boostcamp.jr.thinktank.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by jr on 2017-02-15.
 */

public class NaverRestClient<T> {

    private T service;
    private String baseUrl = "https://openapi.naver.com/";

    private final String CLIENT_ID = BuildConfig.MY_CLIENT_ID;
//    private final String CLIENT_SECRET = BuildConfig.MY_CLIENT_SECRET;

    public T getClient(Class<? extends T> type) {

        if (service == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {

                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    Request request = original.newBuilder()
                            .header("X-Naver-Client-Id", CLIENT_ID)
//                            .header("X-Naver-Client-Secret", CLIENT_SECRET)
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                }

            }).build();

            Retrofit client = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = client.create(type);
        }

        return service;
    }

    public interface KeywordService {
        @GET("v1/{api}/{response}")
        Call<ResponseFromNaver> getKeywordsFromNaver(@Path("api") String kind,
                                                     @Path("response") String response,
                                                     @Query("query") String query);
    }
}
