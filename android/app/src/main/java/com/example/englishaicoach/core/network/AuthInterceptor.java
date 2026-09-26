package com.example.englishaicoach.core.network;

import androidx.annotation.NonNull;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.auth.TokenSession;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {
    private final SessionManager sessions;

    public AuthInterceptor(SessionManager sessions) {
        this.sessions = sessions;
    }

    @NonNull @Override public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        TokenSession bound = request.tag(TokenSession.class);
        if (bound == null || isPublicAuth(request)) return chain.proceed(request);
        if (!sessions.isCurrentSession(bound.sessionId())) {
            throw new IOException("Session changed before request execution");
        }
        return chain.proceed(request.newBuilder()
                .header("Authorization", "Bearer " + bound.accessToken()).build());
    }

    private static boolean isPublicAuth(Request request) {
        String path = request.url().encodedPath();
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/google")
                || path.equals("/api/v1/auth/refresh");
    }
}
