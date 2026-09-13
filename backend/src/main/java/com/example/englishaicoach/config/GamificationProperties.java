package com.example.englishaicoach.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Giá trị XP cố định của {@code gamification-v1}.
 * Các trường XP dùng đơn vị điểm; {@code perfectQuizMinimumQuestions} dùng số câu hỏi.
 * Đây là default của approved baseline, không phải bảng thưởng có thể tự ý tuning.
 */
@ConfigurationProperties("app.gamification")
public record GamificationProperties(
        String algorithmVersion,
        int correctLearningAttemptXp,
        int correctQuizAnswerXp,
        int completedLearningSessionXp,
        int completedDailyPlanXp,
        int maintainedStreakXp,
        int xpPerLevel,
        int perfectQuizMinimumQuestions) {
}
