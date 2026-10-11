package com.example.englishaicoach.data.api;

import com.google.gson.JsonElement;
import com.example.englishaicoach.domain.model.OnboardingData.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface OnboardingApi {
    @GET("users/me") Call<JsonElement> user();
    @GET("users/me/profile") Call<JsonElement> profile();
    @GET("goals") Call<JsonElement> catalog();
    @GET("users/me/goals") Call<JsonElement> selection();
    @PUT("users/me/profile") Call<JsonElement> saveProfile(@Body Profile profile);
    @PUT("users/me/goals") Call<JsonElement> saveSelection(@Body Selection selection);
}
