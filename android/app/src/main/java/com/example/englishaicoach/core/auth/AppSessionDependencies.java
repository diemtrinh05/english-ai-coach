package com.example.englishaicoach.core.auth;

import android.content.Context;
import com.example.englishaicoach.BuildConfig;
import com.example.englishaicoach.core.network.ApiClient;

public final class AppSessionDependencies {
    private static AppSessionDependencies instance;
    public final SessionManager sessions;
    public final ApiClient api;
    public final com.example.englishaicoach.domain.usecase.RestoreSessionUseCase restoreSession;
    public final com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase submitAuthForm;
    private AppSessionDependencies(Context context) {
        sessions = new SessionManager(new EncryptedTokenStore(context.getApplicationContext()));
        api = new ApiClient(BuildConfig.API_BASE_URL, sessions);
        restoreSession = new com.example.englishaicoach.domain.usecase.RestoreSessionUseCase(
                new com.example.englishaicoach.data.repository.RetrofitAuthSessionRepository(sessions,
                        api.create(com.example.englishaicoach.data.api.CurrentUserApi.class)));
        submitAuthForm = createForms(sessions, api);
    }
    AppSessionDependencies(SessionManager sessions, ApiClient api,
            com.example.englishaicoach.domain.usecase.RestoreSessionUseCase restoreSession) {
        this(sessions, api, restoreSession, createForms(sessions, api));
    }
    AppSessionDependencies(SessionManager sessions, ApiClient api,
            com.example.englishaicoach.domain.usecase.RestoreSessionUseCase restoreSession,
            com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase forms) {
        this.sessions = sessions;
        this.api = api;
        this.restoreSession = restoreSession;
        submitAuthForm = forms;
    }
    private static com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase createForms(SessionManager sessions, ApiClient api) {
        return new com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase(
                new com.example.englishaicoach.data.repository.RetrofitAuthFormsRepository(sessions,
                        api.createPublicAuth(com.example.englishaicoach.data.api.AuthFormsApi.class), api.errors()));
    }
    public static synchronized AppSessionDependencies get(Context context) {
        if (instance == null) instance = new AppSessionDependencies(context);
        return instance;
    }
}
