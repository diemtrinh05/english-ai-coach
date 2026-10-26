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
    public final com.example.englishaicoach.domain.usecase.ConfigureOnboardingUseCase configureOnboarding;
    private AppSessionDependencies(Context context) {
        sessions = new SessionManager(new EncryptedTokenStore(context.getApplicationContext()));
        new com.example.englishaicoach.data.local.EncryptedAssessmentCheckpointStore(context).bind(sessions);
        api = new ApiClient(BuildConfig.API_BASE_URL, sessions);
        restoreSession = new com.example.englishaicoach.domain.usecase.RestoreSessionUseCase(
                new com.example.englishaicoach.data.repository.RetrofitAuthSessionRepository(sessions,
                        api.create(com.example.englishaicoach.data.api.CurrentUserApi.class)));
        submitAuthForm = createForms(sessions, api);
        configureOnboarding = createOnboarding(sessions, api);
    }
    AppSessionDependencies(SessionManager sessions, ApiClient api,
            com.example.englishaicoach.domain.usecase.RestoreSessionUseCase restoreSession) {
        this(sessions, api, restoreSession, createForms(sessions, api));
    }
    AppSessionDependencies(SessionManager sessions, ApiClient api,
            com.example.englishaicoach.domain.usecase.RestoreSessionUseCase restoreSession,
            com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase forms) {
        this(sessions, api, restoreSession, forms, createOnboarding(sessions, api));
    }
    AppSessionDependencies(SessionManager sessions, ApiClient api,
            com.example.englishaicoach.domain.usecase.RestoreSessionUseCase restoreSession,
            com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase forms,
            com.example.englishaicoach.domain.usecase.ConfigureOnboardingUseCase onboarding) {
        this.sessions = sessions;
        this.api = api;
        this.restoreSession = restoreSession;
        submitAuthForm = forms;
        configureOnboarding = onboarding;
    }
    private static com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase createForms(SessionManager sessions, ApiClient api) {
        return new com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase(
                new com.example.englishaicoach.data.repository.RetrofitAuthFormsRepository(sessions,
                        api.createPublicAuth(com.example.englishaicoach.data.api.AuthFormsApi.class), api.errors()));
    }
    private static com.example.englishaicoach.domain.usecase.ConfigureOnboardingUseCase createOnboarding(SessionManager sessions, ApiClient api) {
        return new com.example.englishaicoach.domain.usecase.ConfigureOnboardingUseCase(
                new com.example.englishaicoach.data.repository.RetrofitOnboardingRepository(sessions,
                        api.createExplicitRequests(com.example.englishaicoach.data.api.OnboardingApi.class), api.errors()));
    }
    public static synchronized AppSessionDependencies get(Context context) {
        if (instance == null) instance = new AppSessionDependencies(context);
        return instance;
    }
}
