package com.example.englishaicoach.core.storage;

public final class ReadOnlyCachePolicy {
    public enum Content {
        GOALS, CEFR_LEVELS, TOPICS, VOCABULARY_METADATA, APPROVED_REUSABLE_CONTENT,
        SRS, PROGRESS, XP, STREAK, ATTEMPT_HISTORY
    }

    private ReadOnlyCachePolicy() { }

    public static boolean canDisplayOffline(Content content) {
        switch (content) {
            case GOALS:
            case CEFR_LEVELS:
            case TOPICS:
            case VOCABULARY_METADATA:
            case APPROVED_REUSABLE_CONTENT:
                return true;
            default:
                return false;
        }
    }
}
