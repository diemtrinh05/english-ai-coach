package com.example.englishaicoach.domain.usecase;

import com.example.englishaicoach.domain.repository.AuthSessionRepository;

public final class RestoreSessionUseCase {
    private final AuthSessionRepository repository;
    public RestoreSessionUseCase(AuthSessionRepository repository) { this.repository = repository; }
    public AuthSessionRepository.Cancelable restore(AuthSessionRepository.Callback callback) {
        return repository.restore(callback);
    }
}
