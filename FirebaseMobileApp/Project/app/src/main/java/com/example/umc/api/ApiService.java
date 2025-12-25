package com.example.umc.api;

import com.example.umc.dto.PaymentRequest;
import com.example.umc.dto.PaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ApiService{
    @POST("api/vnpay/create") Call<PaymentResponse> createPayment(@Body PaymentRequest req);
}