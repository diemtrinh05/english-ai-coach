package com.example.englishaicoach.domain.usecase;

import com.example.englishaicoach.domain.repository.OnboardingRepository;
import com.example.englishaicoach.domain.model.OnboardingData.*;

public final class ConfigureOnboardingUseCase {
    private final OnboardingRepository repository;
    public ConfigureOnboardingUseCase(OnboardingRepository repository) { this.repository = repository; }
    public OnboardingRepository.Cancelable load(OnboardingRepository.Callback callback) { return repository.load(callback); }
    public OnboardingRepository.Cancelable save(Profile profile, Selection selection, boolean saved, OnboardingRepository.Callback callback) {
        return repository.save(profile, selection, saved, callback);
    }
}
