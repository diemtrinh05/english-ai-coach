package com.example.englishaicoach.domain.model;
import java.math.BigDecimal;
public final class VocabularyProgress {
    public final String vocabularyId, status, nextReviewAt;
    public final BigDecimal easeFactor;
    public final int intervalDays, repetitions, correctCount, incorrectCount;
    public VocabularyProgress(String id, String status, String next, BigDecimal ease, int interval,
            int repetitions, int correct, int incorrect) {
        vocabularyId = id; this.status = status; nextReviewAt = next; easeFactor = ease;
        intervalDays = interval; this.repetitions = repetitions; correctCount = correct; incorrectCount = incorrect;
    }
}
