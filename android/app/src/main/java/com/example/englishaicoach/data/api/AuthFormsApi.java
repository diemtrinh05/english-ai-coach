package com.example.englishaicoach.data.api;

import com.example.englishaicoach.data.dto.AuthFormDtos;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthFormsApi {
    @POST("auth/login") Call<AuthFormDtos.Response> login(@Body AuthFormDtos.Login request);
    @POST("auth/register") Call<AuthFormDtos.Response> register(@Body AuthFormDtos.Register request);
}
