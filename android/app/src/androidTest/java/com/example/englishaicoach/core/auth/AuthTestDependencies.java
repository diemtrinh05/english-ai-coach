package com.example.englishaicoach.core.auth;

import com.example.englishaicoach.core.network.ApiClient;
import com.example.englishaicoach.domain.repository.AuthSessionRepository;
import com.example.englishaicoach.domain.usecase.RestoreSessionUseCase;
import java.lang.reflect.Field;

// Fixture chỉ nằm trong test APK; ứng dụng thật luôn dùng kho mã hóa và API thật.
public final class AuthTestDependencies implements AutoCloseable {
    public final SessionManager sessions;
    private final Field field;
    private final Object previous;
    public AuthTestDependencies(boolean authenticated) throws Exception {
        field = AppSessionDependencies.class.getDeclaredField("instance");
        field.setAccessible(true);
        previous = field.get(null);
        sessions = new SessionManager(new TokenStore() {
            TokenSession value;
            public TokenSession read() { return value; }
            public void write(TokenSession session) { value = session; }
        });
        if (authenticated) sessions.start("test-access", "test-refresh");
        RestoreSessionUseCase restore = new RestoreSessionUseCase(callback -> {
            callback.completed(sessions.current() == null ? AuthSessionRepository.Result.UNAUTHENTICATED
                    : AuthSessionRepository.Result.AUTHENTICATED);
            return () -> { };
        });
        field.set(null, new AppSessionDependencies(sessions,
                new ApiClient("https://example.invalid/api/v1/", sessions), restore));
    }
    @Override public void close() throws Exception { field.set(null, previous); }
}
