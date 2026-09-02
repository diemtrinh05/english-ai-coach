package com.example.englishaicoach.config;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nhóm tham số typed cho các thuật toán personalization V1 đã được phê duyệt.
 * Record chỉ mô tả input cấu hình; việc tính weakness, forgetting risk, recommendation
 * và daily workload thuộc các task business tương ứng.
 */
@ConfigurationProperties("app.personalization")
public record PersonalizationProperties(
        String weaknessAlgorithmVersion,
        String forgettingRiskAlgorithmVersion,
        String dailyPlanAlgorithmVersion,
        Weakness weakness,
        ForgettingRisk forgettingRisk,
        Recommendation recommendation,
        Workload workload,
        Allocation allocation) {

    /**
     * Cửa sổ lịch sử, ngưỡng và trọng số của {@code weakness-rule-v1}.
     * Các cửa sổ dùng số attempt, thời gian dùng {@link Duration}, tỷ lệ nằm trong 0..1;
     * bốn trọng số thành phần của approved baseline có tổng bằng 1.
     */
    public record Weakness(
            int historyWindow,
            int recentWindow,
            int minimumAttempts,
            Duration responseTimeBaseline,
            Duration responseTimeRange,
            BigDecimal weakAccuracyThreshold,
            int consecutiveFailuresThreshold,
            Duration slowRecallThreshold,
            BigDecimal lowAnswerQualityThreshold,
            BigDecimal errorRateWeight,
            BigDecimal responseTimeWeight,
            BigDecimal lowQualityWeight,
            BigDecimal recentFailureWeight) {
    }

    /**
     * Trọng số và ngưỡng phân loại của {@code forgetting-risk-v1}.
     * Quality dùng thang 0..5, repetition là số lần, các trọng số/ngưỡng risk không có
     * đơn vị và nằm trong 0..1; năm trọng số của approved baseline có tổng bằng 1.
     */
    public record ForgettingRisk(
            BigDecimal defaultLastQuality,
            int maximumRepetitions,
            BigDecimal timePressureWeight,
            BigDecimal errorRateWeight,
            BigDecimal qualityPenaltyWeight,
            BigDecimal repetitionPenaltyWeight,
            BigDecimal responsePenaltyWeight,
            BigDecimal mediumThreshold,
            BigDecimal highThreshold) {
    }

    /**
     * Các giới hạn xếp hạng recommendation; số lượng dùng đơn vị item/level, cap và
     * multiplier là tỷ lệ không đơn vị. Các default thuộc approved baseline V1.
     */
    public record Recommendation(
            int lowerLevelFallback,
            int topicDiversityMinimumNewWords,
            BigDecimal topicDiversityCap,
            BigDecimal nonPrimaryGoalMultiplier) {
    }

    /**
     * Cửa sổ đánh giá và guard điều chỉnh daily workload.
     * Threshold là tỷ lệ 0..1, change dùng điểm phần trăm nguyên, năng suất dùng
     * unit/phút; {@code maximumIncreasePercent} giữ giới hạn tăng tối đa 20% của baseline.
     */
    public record Workload(
            int performanceWindowDays,
            int minimumActiveDays,
            BigDecimal highCompletionThreshold,
            BigDecimal highAccuracyThreshold,
            BigDecimal lowCompletionThreshold,
            BigDecimal lowAccuracyThreshold,
            int highChangePercent,
            int lowChangePercent,
            int maximumIncreasePercent,
            int maximumDecreasePercent,
            BigDecimal initialUnitsPerMinute,
            int minimumInitialUnits) {
    }

    /**
     * Tỷ lệ phân bổ review/new/quiz và thời lượng ước tính để dựng Daily Plan.
     * Mỗi bộ tỷ lệ dùng phần trăm nguyên và phải có tổng 100; thời lượng dùng giây/item.
     */
    public record Allocation(
            int highBacklogMinimum,
            BigDecimal highBacklogMultiplier,
            int highBacklogReviewPercent,
            int highBacklogNewPercent,
            int highBacklogQuizPercent,
            int highRetentionReviewPercent,
            int highRetentionNewPercent,
            int highRetentionQuizPercent,
            int normalReviewPercent,
            int normalNewPercent,
            int normalQuizPercent,
            int newItemEstimatedSeconds,
            int reviewItemEstimatedSeconds,
            int quizQuestionEstimatedSeconds) {
    }
}
