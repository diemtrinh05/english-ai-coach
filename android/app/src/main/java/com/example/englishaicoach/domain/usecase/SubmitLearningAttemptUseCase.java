package com.example.englishaicoach.domain.usecase;

import com.example.englishaicoach.domain.model.LearningAttempt;
import com.example.englishaicoach.domain.repository.*;

public final class SubmitLearningAttemptUseCase {
    private final LearningAttemptRepository repository;
    public SubmitLearningAttemptUseCase(LearningAttemptRepository repository) { this.repository = repository; }
    public LearningSessionRepository.Cancelable submit(String owner, LearningAttempt.Request request,
            LearningAttemptRepository.Callback callback) { return repository.submit(owner, request, callback); }
    public LearningSessionRepository.Cancelable refresh(String owner, String session, String vocabulary,
            LearningAttemptRepository.RefreshCallback callback) { return repository.refresh(owner, session, vocabulary, callback); }
}
