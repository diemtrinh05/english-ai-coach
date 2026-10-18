package com.example.englishaicoach.data.api;

import com.google.gson.JsonElement;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface LearningSessionApi {
    @GET("users/me") Call<JsonElement> user();
    @POST("learning/sessions") Call<JsonElement> start(@Body Map<String, String> body);
    @GET("learning/sessions/{sessionId}") Call<JsonElement> get(@Path("sessionId") String id);
}
