package com.example.englishaicoach.feature.assessment;

import android.content.Context;
import com.example.englishaicoach.core.auth.AppSessionDependencies;
import com.example.englishaicoach.data.api.AssessmentApi;
import com.example.englishaicoach.data.repository.RetrofitAssessmentRepository;
import com.example.englishaicoach.domain.usecase.AssessmentUseCase;

public final class AssessmentDependencies {
    private static AssessmentUseCase useCase;
    private AssessmentDependencies() { }
    public static synchronized AssessmentUseCase useCase(Context context) {
        if (useCase == null) {
            AppSessionDependencies dependencies = AppSessionDependencies.get(context);
            useCase = new AssessmentUseCase(new RetrofitAssessmentRepository(dependencies.sessions,
                    dependencies.api.createExplicitRequests(AssessmentApi.class), dependencies.api.errors()));
        }
        return useCase;
    }
}
