package com.example.englishaicoach.core.auth;

import android.content.Context;
import com.example.englishaicoach.BuildConfig;
import com.example.englishaicoach.core.network.ApiClient;

public final class AppSessionDependencies {
    private static AppSessionDependencies instance;
    public final SessionManager sessions;
    public final ApiClient api;
    public final com.example.englishaicoach.domain.usecase.RestoreSessionUseCase restoreSession;
    private AppSessionDependencies(Context context) {
        sessions = new SessionManager(new EncryptedTokenStore(context.getApplicationContext()));
        api = new ApiClient(BuildConfig.API_BASE_URL, sessions);
        restoreSession = new com.example.englishaicoach.domain.usecase.RestoreSessionUseCase(
                new com.example.englishaicoach.data.repository.RetrofitAuthSessionRepository(sessions,
                        api.create(com.example.englishaicoach.data.api.CurrentUserApi.class)));
    }
    AppSessionDependencies(SessionManager sessions, ApiClient api,
            com.example.englishaicoach.domain.usecase.RestoreSessionUseCase restoreSession) {
        this.sessions = sessions;
        this.api = api;
        this.restoreSession = restoreSession;
    }
    public static synchronized AppSessionDependencies get(Context context) {
        if (instance == null) instance = new AppSessionDependencies(context);
        return instance;
    }
}
