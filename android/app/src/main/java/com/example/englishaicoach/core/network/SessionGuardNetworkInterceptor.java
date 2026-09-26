package com.example.englishaicoach.core.network;

import androidx.annotation.NonNull;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.auth.TokenSession;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public final class SessionGuardNetworkInterceptor implements Interceptor {
    private final SessionManager sessions;

    public SessionGuardNetworkInterceptor(SessionManager sessions) {
        this.sessions = sessions;
    }

    @NonNull @Override public Response intercept(@NonNull Chain chain) throws IOException {
        TokenSession bound = chain.request().tag(TokenSession.class);
        if (bound != null && !sessions.isCurrentSession(bound.sessionId())) {
            throw new IOException("Session changed before network exchange");
        }
        return chain.proceed(chain.request());
    }
}
