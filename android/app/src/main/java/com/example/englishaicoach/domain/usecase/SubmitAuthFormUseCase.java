package com.example.englishaicoach.domain.usecase;

import com.example.englishaicoach.domain.repository.AuthFormsRepository;

public final class SubmitAuthFormUseCase {
    private final AuthFormsRepository repository;
    public SubmitAuthFormUseCase(AuthFormsRepository repository) { this.repository = repository; }
    public AuthFormsRepository.Cancelable submit(boolean register, String fullName, String email,
            String password, AuthFormsRepository.Callback callback) {
        return repository.submit(register, fullName, email, password, callback);
    }
}
