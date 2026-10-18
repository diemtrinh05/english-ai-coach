package com.example.englishaicoach.data.api;
import com.google.gson.JsonElement;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;
public interface ReviewLearningApi {
    @GET("users/me") Call<JsonElement> user();
    @GET("learning/reviews") Call<JsonElement> reviews();
    @GET("vocabulary/{vocabularyId}/progress") Call<JsonElement> progress(@Path("vocabularyId") String id);
    @POST("learning/sessions/{sessionId}/complete") Call<JsonElement> complete(@Path("sessionId") String id, @Body Map<String,String> body);
    @GET("learning/sessions/{sessionId}") Call<JsonElement> session(@Path("sessionId") String id);
}
