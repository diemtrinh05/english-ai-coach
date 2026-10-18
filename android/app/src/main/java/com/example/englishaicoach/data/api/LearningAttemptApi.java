package com.example.englishaicoach.data.api;

import com.example.englishaicoach.domain.model.LearningAttempt;
import com.google.gson.JsonElement;
import retrofit2.Call;
import retrofit2.http.*;

public interface LearningAttemptApi {
    @GET("users/me") Call<JsonElement> user();
    @POST("learning/attempts") Call<JsonElement> submit(@Body LearningAttempt.Request request);
    @GET("learning/sessions/{id}") Call<JsonElement> session(@Path("id") String id);
    @GET("vocabulary/{id}/progress") Call<JsonElement> progress(@Path("id") String id);
}
