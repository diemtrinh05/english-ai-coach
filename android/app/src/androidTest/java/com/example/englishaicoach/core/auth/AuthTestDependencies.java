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
    private final Field gateField;
    private final Object previousGate;
    public AuthTestDependencies(boolean authenticated) throws Exception {
        this(authenticated, null);
    }
    public AuthTestDependencies(boolean authenticated,
            com.example.englishaicoach.domain.usecase.ConfigureOnboardingUseCase onboarding) throws Exception {
        field = AppSessionDependencies.class.getDeclaredField("instance");
        field.setAccessible(true);
        previous = field.get(null);
        gateField = com.example.englishaicoach.feature.onboarding.OnboardingGateDependencies.class.getDeclaredField("useCase");
        gateField.setAccessible(true); previousGate = gateField.get(null);
        gateField.set(null, new com.example.englishaicoach.domain.usecase.ResolveOnboardingUseCase((owner, callback) -> {
            callback.completed(com.example.englishaicoach.domain.model.OnboardingRoute.HOME, null); return () -> { };
        }));
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
        ApiClient api = new ApiClient("https://example.invalid/api/v1/", sessions);
        field.set(null, onboarding == null ? new AppSessionDependencies(sessions, api, restore)
                : new AppSessionDependencies(sessions, api, restore,
                    new com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase((register, name, email, password, callback) -> () -> { }), onboarding));
    }
    @Override public void close() throws Exception { field.set(null, previous); gateField.set(null, previousGate); }
}
