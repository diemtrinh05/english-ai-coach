package com.example.englishaicoach.domain.model;

import java.util.UUID;

public final class LearningAttempt {
    public static final class Request {
        public final String sessionId, vocabularyId, attemptType, eventId;
        public final int responseTimeMs, answerQuality;
        public Request(String session, String vocabulary, int time, int quality, String event) {
            UUID.fromString(session); UUID.fromString(vocabulary); UUID.fromString(event);
            if (time < 0 || quality < 0 || quality > 5) throw new IllegalArgumentException();
            sessionId = session; vocabularyId = vocabulary; responseTimeMs = time;
            answerQuality = quality; eventId = event; attemptType = "FLASHCARD";
        }
    }
    public final String attemptId, nextReviewAt;
    public final boolean isCorrect;
    public final int answerQuality, intervalDays;
    public LearningAttempt(String id, boolean correct, int quality, String next, int interval) {
        attemptId = id; isCorrect = correct; answerQuality = quality;
        nextReviewAt = next; intervalDays = interval;
    }
}
