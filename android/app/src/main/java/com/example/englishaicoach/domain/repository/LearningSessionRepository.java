package com.example.englishaicoach.domain.repository;

import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.LearningSession;
import com.example.englishaicoach.domain.model.VocabularyItem;

public interface LearningSessionRepository {
    interface Cancelable { void cancel(); }
    interface Callback { void completed(Result result); }
    final class Result {
        public final LearningSession session;
        public final VocabularyItem card;
        public final ApiError error;
        public Result(LearningSession session, VocabularyItem card, ApiError error) {
            this.session = session; this.card = card; this.error = error;
        }
    }
    Cancelable load(String owner, String eventId, String vocabularyId, String sessionId,
                    boolean start, Callback callback);
}
