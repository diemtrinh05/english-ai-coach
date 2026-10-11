package com.example.englishaicoach.data.api;

import com.example.englishaicoach.data.dto.CurrentUserDto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CurrentUserApi {
    @GET("users/me") Call<CurrentUserDto> currentUser();
}
