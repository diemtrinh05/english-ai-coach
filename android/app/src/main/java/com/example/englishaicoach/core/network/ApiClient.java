package com.example.englishaicoach.core.network;

import com.example.englishaicoach.core.auth.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private final Retrofit retrofit;
    private final Retrofit publicAuth;
    private final Retrofit explicitRequests;
    private final ApiErrorParser errorParser = new ApiErrorParser();

    public ApiClient(String baseUrl, SessionManager sessions) {
        if (baseUrl == null || !baseUrl.startsWith("https://") || !baseUrl.endsWith("/api/v1/")) {
            throw new IllegalArgumentException("HTTPS API v1 base URL is required");
        }
        OkHttpClient refreshClient = refreshHttpClient();
        Retrofit refreshRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(refreshClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RefreshService refreshService = refreshRetrofit.create(RefreshService.class);
        publicAuth = refreshRetrofit;

        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .callTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(sessions))
                .addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions))
                .authenticator(new SerializedRefreshAuthenticator(sessions, refreshService))
                .build();
        explicitRequests = new Retrofit.Builder().baseUrl(baseUrl)
                .callFactory(new SessionBindingCallFactory(client.newBuilder().retryOnConnectionFailure(false).build(), sessions))
                .addConverterFactory(GsonConverterFactory.create()).build();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .callFactory(new SessionBindingCallFactory(client, sessions))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    static OkHttpClient refreshHttpClient() {
        // Token single-use không được tự gửi lại sau lỗi kết nối chưa rõ kết quả.
        return new OkHttpClient.Builder().retryOnConnectionFailure(false)
                .followRedirects(false).callTimeout(30, TimeUnit.SECONDS).build();
    }

    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }

    // Mutation auth không tự retry và không gắn credential của phiên đang tồn tại.
    public <T> T createPublicAuth(Class<T> service) { return publicAuth.create(service); }

    // Hai PUT thiết lập chỉ retry khi người dùng yêu cầu; vẫn dùng session/refresh guards chung.
    public <T> T createExplicitRequests(Class<T> service) { return explicitRequests.create(service); }

    public ApiErrorParser errors() {
        return errorParser;
    }
}
