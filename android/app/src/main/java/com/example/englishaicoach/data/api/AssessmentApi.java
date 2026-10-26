package com.example.englishaicoach.data.api;

import com.example.englishaicoach.domain.model.Assessment;
import com.google.gson.JsonElement;
import retrofit2.Call;
import retrofit2.http.*;

public interface AssessmentApi {
    @GET("users/me") Call<JsonElement> user();
    @POST("assessments") Call<JsonElement> start(@Body Assessment.Start request);
    @GET("assessments/{id}") Call<JsonElement> result(@Path("id") String id);
    @GET("assessments/{id}/next-question") Call<JsonElement> question(@Path("id") String id);
    @POST("assessments/{id}/answers") Call<JsonElement> answer(@Path("id") String id, @Body Assessment.Answer request);
}
