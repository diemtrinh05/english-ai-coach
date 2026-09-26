package com.example.englishaicoach.core.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.auth.TokenSession;

import okhttp3.Call;
import okhttp3.Request;

public final class SessionBindingCallFactory implements Call.Factory {
    private final Call.Factory delegate;
    private final SessionManager sessions;

    public SessionBindingCallFactory(Call.Factory delegate, SessionManager sessions) {
        this.delegate = delegate;
        this.sessions = sessions;
    }

    @NonNull @Override public Call newCall(@NonNull Request request) {
        // Gắn phiên tại lúc tạo Call để request cũ không thể chạy lại dưới tài khoản mới.
        TokenSession bound = sessions.current();
        return delegate.newCall(request.newBuilder().tag(TokenSession.class, bound).build());
    }
}
