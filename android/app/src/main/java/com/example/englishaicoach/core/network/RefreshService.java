package com.example.englishaicoach.core.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import okhttp3.ResponseBody;

public interface RefreshService {
    // Giữ body thô để tách lỗi kết nối tạm thời khỏi lỗi JSON 200 không hợp lệ.
    @POST("auth/refresh")
    Call<ResponseBody> refresh(@Body RefreshRequest request);
}
