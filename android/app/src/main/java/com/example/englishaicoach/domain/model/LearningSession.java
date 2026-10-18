package com.example.englishaicoach.domain.model;

public final class LearningSession {
    public final String sessionId, sessionType, startedAt, endedAt;
    public LearningSession(String id, String type, String start, String end) {
        sessionId = id; sessionType = type; startedAt = start; endedAt = end;
    }
    public boolean open() { return endedAt == null; }
}
