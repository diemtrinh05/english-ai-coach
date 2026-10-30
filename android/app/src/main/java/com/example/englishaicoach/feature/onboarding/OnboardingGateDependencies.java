package com.example.englishaicoach.feature.onboarding;

import android.content.Context;
import com.example.englishaicoach.core.auth.AppSessionDependencies;
import com.example.englishaicoach.data.api.OnboardingApi;
import com.example.englishaicoach.data.repository.RetrofitOnboardingGateRepository;
import com.example.englishaicoach.domain.usecase.ResolveOnboardingUseCase;

public final class OnboardingGateDependencies {
    private static ResolveOnboardingUseCase useCase;
    private OnboardingGateDependencies() { }
    public static synchronized ResolveOnboardingUseCase useCase(Context context) {
        if (useCase == null) {
            AppSessionDependencies dependencies = AppSessionDependencies.get(context);
            useCase = new ResolveOnboardingUseCase(new RetrofitOnboardingGateRepository(dependencies.sessions,
                    dependencies.api.createExplicitRequests(OnboardingApi.class), dependencies.api.errors()));
        }
        return useCase;
    }
}
