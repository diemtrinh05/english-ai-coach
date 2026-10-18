package com.example.englishaicoach.domain.usecase;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.*;
import java.util.List;
public final class ReviewLearningUseCase {
    private final ReviewLearningRepository repository;
    public ReviewLearningUseCase(ReviewLearningRepository repository) { this.repository = repository; }
    public LearningSessionRepository.Cancelable reviews(String owner, ReviewLearningRepository.Callback<List<ReviewItem>> callback) {
        return repository.reviews(owner, callback);
    }
    public LearningSessionRepository.Cancelable progress(String owner, String vocabulary, ReviewLearningRepository.Callback<VocabularyProgress> callback) {
        return repository.progress(owner, vocabulary, callback);
    }
    public LearningSessionRepository.Cancelable complete(String owner, String session, String event, ReviewLearningRepository.Callback<LearningSummary> callback) {
        return repository.complete(owner, session, event, callback);
    }
    public LearningSessionRepository.Cancelable summary(String owner, String session, ReviewLearningRepository.Callback<LearningSummary> callback) {
        return repository.summary(owner, session, callback);
    }
}
