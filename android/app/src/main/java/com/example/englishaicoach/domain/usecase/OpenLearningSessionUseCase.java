package com.example.englishaicoach.domain.usecase;

import com.example.englishaicoach.domain.repository.LearningSessionRepository;

public final class OpenLearningSessionUseCase {
    private final LearningSessionRepository repository;
    public OpenLearningSessionUseCase(LearningSessionRepository repository) { this.repository = repository; }
    public LearningSessionRepository.Cancelable load(String owner, String event, String vocabulary,
            String session, boolean start, String type, LearningSessionRepository.Callback callback) {
        return repository.load(owner, event, vocabulary, session, start, type, callback);
    }
    public LearningSessionRepository.Cancelable load(String owner, String event, String vocabulary,
            String session, boolean start, LearningSessionRepository.Callback callback) {
        return repository.load(owner, event, vocabulary, session, start, callback);
    }
}
