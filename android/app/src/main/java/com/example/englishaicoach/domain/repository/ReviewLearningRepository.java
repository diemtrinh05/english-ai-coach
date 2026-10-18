package com.example.englishaicoach.domain.repository;
import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.*;
import java.util.List;
public interface ReviewLearningRepository {
    interface Callback<T> { void completed(T value, ApiError error); }
    LearningSessionRepository.Cancelable reviews(String owner, Callback<List<ReviewItem>> callback);
    LearningSessionRepository.Cancelable progress(String owner, String vocabulary, Callback<VocabularyProgress> callback);
    LearningSessionRepository.Cancelable complete(String owner, String session, String event, Callback<LearningSummary> callback);
    LearningSessionRepository.Cancelable summary(String owner, String session, Callback<LearningSummary> callback);
}
