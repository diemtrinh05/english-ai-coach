package com.example.englishaicoach.gamification;

import com.example.englishaicoach.config.GamificationProperties;

public enum XpReason {
    LEARNING_CORRECT("session_attempts"),
    QUIZ_CORRECT("quiz_attempt_answers"),
    SESSION_COMPLETED("learning_sessions"),
    DAILY_PLAN_COMPLETED("daily_plans"),
    STREAK_MAINTAINED("streaks");

    private final String referenceType;

    XpReason(String referenceType) {
        this.referenceType = referenceType;
    }

    public String referenceType() {
        return referenceType;
    }

    public int amount(GamificationProperties properties) {
        return switch (this) {
            case LEARNING_CORRECT -> properties.correctLearningAttemptXp();
            case QUIZ_CORRECT -> properties.correctQuizAnswerXp();
            case SESSION_COMPLETED -> properties.completedLearningSessionXp();
            case DAILY_PLAN_COMPLETED -> properties.completedDailyPlanXp();
            case STREAK_MAINTAINED -> properties.maintainedStreakXp();
        };
    }
}
