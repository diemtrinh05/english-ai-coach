package com.example.englishaicoach.domain.repository;

import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.OnboardingRoute;

public interface OnboardingGateRepository {
    interface Cancelable { void cancel(); }
    interface Callback { void completed(OnboardingRoute route, ApiError error); }
    Cancelable resolve(String owner, Callback callback);
}
