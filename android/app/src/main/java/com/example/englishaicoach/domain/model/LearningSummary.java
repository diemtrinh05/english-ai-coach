package com.example.englishaicoach.domain.model;
import java.math.BigDecimal;
public final class LearningSummary {
    public final String sessionId, sessionType, startedAt, endedAt;
    public final int wordsStudiedCount;
    public final BigDecimal accuracyPercent;
    public LearningSummary(String id, String type, String start, String end, int words, BigDecimal accuracy) {
        sessionId = id; sessionType = type; startedAt = start; endedAt = end;
        wordsStudiedCount = words; accuracyPercent = accuracy;
    }
}
