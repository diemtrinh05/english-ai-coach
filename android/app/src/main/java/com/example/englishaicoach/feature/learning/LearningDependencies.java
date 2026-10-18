package com.example.englishaicoach.feature.learning;

import android.content.Context;
import com.example.englishaicoach.core.auth.AppSessionDependencies;
import com.example.englishaicoach.data.api.*;
import com.example.englishaicoach.data.repository.RetrofitLearningSessionRepository;
import com.example.englishaicoach.domain.usecase.OpenLearningSessionUseCase;

public final class LearningDependencies {
    private static OpenLearningSessionUseCase useCase;
    private LearningDependencies() { }
    public static synchronized OpenLearningSessionUseCase useCase(Context context) {
        if (useCase != null) return useCase;
        AppSessionDependencies dependencies = AppSessionDependencies.get(context);
        useCase = new OpenLearningSessionUseCase(new RetrofitLearningSessionRepository(dependencies.sessions,
                dependencies.api.createExplicitRequests(LearningSessionApi.class),
                dependencies.api.create(VocabularyApi.class), dependencies.api.errors()));
        return useCase;
    }
}
