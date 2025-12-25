package com.example.umc.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Render backend
    private static final String BASE_URL =
            "https://vnpayspringboot-3.onrender.com/";

    private static Retrofit retrofit;

    public static Retrofit get() {
        if (retrofit == null) {

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS) // kết nối
                    .readTimeout(60, TimeUnit.SECONDS)    // đọc response
                    .writeTimeout(60, TimeUnit.SECONDS)   // gửi request
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // ⭐ QUAN TRỌNG
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
