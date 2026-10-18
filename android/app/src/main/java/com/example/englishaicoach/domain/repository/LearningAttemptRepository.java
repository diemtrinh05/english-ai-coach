package com.example.englishaicoach.domain.repository;

import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.LearningAttempt;

public interface LearningAttemptRepository {
    interface Callback { void completed(LearningAttempt result, ApiError error); }
    interface RefreshCallback { void completed(boolean open, ApiError error); }
    LearningSessionRepository.Cancelable submit(String owner, LearningAttempt.Request request, Callback callback);
    LearningSessionRepository.Cancelable refresh(String owner, String session, String vocabulary, RefreshCallback callback);
}
