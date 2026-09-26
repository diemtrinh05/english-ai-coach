package com.example.englishaicoach.core.network;

import com.example.englishaicoach.core.auth.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private final Retrofit retrofit;
    private final ApiErrorParser errorParser = new ApiErrorParser();

    public ApiClient(String baseUrl, SessionManager sessions) {
        if (baseUrl == null || !baseUrl.startsWith("https://") || !baseUrl.endsWith("/api/v1/")) {
            throw new IllegalArgumentException("HTTPS API v1 base URL is required");
        }
        OkHttpClient refreshClient = new OkHttpClient.Builder()
                .followRedirects(false)
                .callTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit refreshRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(refreshClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RefreshService refreshService = refreshRetrofit.create(RefreshService.class);

        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .callTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(sessions))
                .addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions))
                .authenticator(new SerializedRefreshAuthenticator(sessions, refreshService))
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .callFactory(new SessionBindingCallFactory(client, sessions))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }

    public ApiErrorParser errors() {
        return errorParser;
    }
}
