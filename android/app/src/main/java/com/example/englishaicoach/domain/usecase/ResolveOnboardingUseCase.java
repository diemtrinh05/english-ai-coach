package com.example.englishaicoach.domain.usecase;

import com.example.englishaicoach.domain.repository.OnboardingGateRepository;

public final class ResolveOnboardingUseCase {
    private final OnboardingGateRepository repository;
    public ResolveOnboardingUseCase(OnboardingGateRepository repository) { this.repository = repository; }
    public OnboardingGateRepository.Cancelable resolve(String owner, OnboardingGateRepository.Callback callback) {
        return repository.resolve(owner, callback);
    }
}
