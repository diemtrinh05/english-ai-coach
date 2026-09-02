package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.englishaicoach.config.AssessmentProperties;
import com.example.englishaicoach.config.GamificationProperties;
import com.example.englishaicoach.config.IdempotencyProperties;
import com.example.englishaicoach.config.NotificationProperties;
import com.example.englishaicoach.config.PersonalizationProperties;
import com.example.englishaicoach.config.SrsProperties;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ConfigurationPropertiesTests {

    @Autowired
    private AssessmentProperties assessment;

    @Autowired
    private SrsProperties srs;

    @Autowired
    private PersonalizationProperties personalization;

    @Autowired
    private GamificationProperties gamification;

    @Autowired
    private NotificationProperties notification;

    @Autowired
    private IdempotencyProperties idempotency;

    @Autowired
    private Environment environment;

    @Test
    void bindsCanonicalAssessmentAndSrsDefaults() {
        assertAll(
                () -> assertEquals("assessment-block-v1", assessment.algorithmVersion()),
                () -> assertEquals(20, assessment.minimumQuestions()),
                () -> assertEquals(50, assessment.maximumQuestions()),
                () -> assertEquals(4, assessment.blockSize()),
                () -> assertEquals(3, assessment.promoteCorrectThreshold()),
                () -> assertEquals(1, assessment.demoteCorrectThreshold()),
                () -> assertEquals(2, assessment.stableHoldBlocksRequired()),
                () -> assertEquals("A1", assessment.startingLevel()),
                () -> assertEquals(4, assessment.optionsPerQuestion()),
                () -> assertEquals(30, assessment.minimumVocabularyPerLevel()),
                () -> assertEquals("sm2-ext-v1", srs.algorithmVersion()),
                () -> assertDecimal("2.50", srs.initialEaseFactor()),
                () -> assertDecimal("1.30", srs.minimumEaseFactor()),
                () -> assertEquals(1, srs.minimumIntervalDays()),
                () -> assertEquals(180, srs.maximumIntervalDays()),
                () -> assertEquals(1, srs.firstSuccessIntervalDays()),
                () -> assertEquals(6, srs.secondSuccessIntervalDays()),
                () -> assertEquals(Duration.ofSeconds(3), srs.responseTimeReference()),
                () -> assertEquals(Duration.ofSeconds(1), srs.responseTimeFloor()),
                () -> assertDecimal("0.85", srs.minimumTimeFactor()),
                () -> assertDecimal("1.10", srs.maximumTimeFactor()),
                () -> assertEquals(3, srs.correctQualityThreshold()),
                () -> assertEquals(4, srs.masteredMinimumQuality()),
                () -> assertEquals(5, srs.masteredMinimumRepetitions()),
                () -> assertEquals(30, srs.masteredMinimumIntervalDays()));
    }

    @Test
    void bindsCanonicalPersonalizationDefaults() {
        PersonalizationProperties.Weakness weakness = personalization.weakness();
        PersonalizationProperties.ForgettingRisk risk = personalization.forgettingRisk();
        PersonalizationProperties.Recommendation recommendation = personalization.recommendation();
        PersonalizationProperties.Workload workload = personalization.workload();
        PersonalizationProperties.Allocation allocation = personalization.allocation();

        assertAll(
                () -> assertEquals("weakness-rule-v1", personalization.weaknessAlgorithmVersion()),
                () -> assertEquals("forgetting-risk-v1", personalization.forgettingRiskAlgorithmVersion()),
                () -> assertEquals("daily-plan-v1", personalization.dailyPlanAlgorithmVersion()),
                () -> assertEquals(10, weakness.historyWindow()),
                () -> assertEquals(5, weakness.recentWindow()),
                () -> assertEquals(3, weakness.minimumAttempts()),
                () -> assertEquals(Duration.ofSeconds(2), weakness.responseTimeBaseline()),
                () -> assertEquals(Duration.ofSeconds(6), weakness.responseTimeRange()),
                () -> assertDecimal("0.60", weakness.weakAccuracyThreshold()),
                () -> assertEquals(2, weakness.consecutiveFailuresThreshold()),
                () -> assertEquals(Duration.ofSeconds(6), weakness.slowRecallThreshold()),
                () -> assertDecimal("3.5", weakness.lowAnswerQualityThreshold()),
                () -> assertDecimal("0.40", weakness.errorRateWeight()),
                () -> assertDecimal("0.20", weakness.responseTimeWeight()),
                () -> assertDecimal("0.20", weakness.lowQualityWeight()),
                () -> assertDecimal("0.20", weakness.recentFailureWeight()),
                () -> assertDecimal("2.5", risk.defaultLastQuality()),
                () -> assertEquals(5, risk.maximumRepetitions()),
                () -> assertDecimal("0.45", risk.timePressureWeight()),
                () -> assertDecimal("0.25", risk.errorRateWeight()),
                () -> assertDecimal("0.15", risk.qualityPenaltyWeight()),
                () -> assertDecimal("0.10", risk.repetitionPenaltyWeight()),
                () -> assertDecimal("0.05", risk.responsePenaltyWeight()),
                () -> assertDecimal("0.40", risk.mediumThreshold()),
                () -> assertDecimal("0.70", risk.highThreshold()),
                () -> assertEquals(1, recommendation.lowerLevelFallback()),
                () -> assertEquals(5, recommendation.topicDiversityMinimumNewWords()),
                () -> assertDecimal("0.40", recommendation.topicDiversityCap()),
                () -> assertDecimal("0.5", recommendation.nonPrimaryGoalMultiplier()),
                () -> assertEquals(7, workload.performanceWindowDays()),
                () -> assertEquals(3, workload.minimumActiveDays()),
                () -> assertDecimal("0.90", workload.highCompletionThreshold()),
                () -> assertDecimal("0.85", workload.highAccuracyThreshold()),
                () -> assertDecimal("0.70", workload.lowCompletionThreshold()),
                () -> assertDecimal("0.60", workload.lowAccuracyThreshold()),
                () -> assertEquals(10, workload.highChangePercent()),
                () -> assertEquals(-20, workload.lowChangePercent()),
                () -> assertEquals(20, workload.maximumIncreasePercent()),
                () -> assertEquals(30, workload.maximumDecreasePercent()),
                () -> assertDecimal("1.5", workload.initialUnitsPerMinute()),
                () -> assertEquals(5, workload.minimumInitialUnits()),
                () -> assertEquals(10, allocation.highBacklogMinimum()),
                () -> assertDecimal("1.5", allocation.highBacklogMultiplier()),
                () -> assertEquals(List.of(80, 15, 5), List.of(
                        allocation.highBacklogReviewPercent(),
                        allocation.highBacklogNewPercent(),
                        allocation.highBacklogQuizPercent())),
                () -> assertEquals(List.of(50, 35, 15), List.of(
                        allocation.highRetentionReviewPercent(),
                        allocation.highRetentionNewPercent(),
                        allocation.highRetentionQuizPercent())),
                () -> assertEquals(List.of(60, 25, 15), List.of(
                        allocation.normalReviewPercent(),
                        allocation.normalNewPercent(),
                        allocation.normalQuizPercent())),
                () -> assertEquals(60, allocation.newItemEstimatedSeconds()),
                () -> assertEquals(30, allocation.reviewItemEstimatedSeconds()),
                () -> assertEquals(45, allocation.quizQuestionEstimatedSeconds()));
    }

    @Test
    void bindsCanonicalGamificationNotificationAndIdempotencyDefaults() {
        assertAll(
                () -> assertEquals("gamification-v1", gamification.algorithmVersion()),
                () -> assertEquals(5, gamification.correctLearningAttemptXp()),
                () -> assertEquals(5, gamification.correctQuizAnswerXp()),
                () -> assertEquals(10, gamification.completedLearningSessionXp()),
                () -> assertEquals(50, gamification.completedDailyPlanXp()),
                () -> assertEquals(10, gamification.maintainedStreakXp()),
                () -> assertEquals(500, gamification.xpPerLevel()),
                () -> assertEquals(5, gamification.perfectQuizMinimumQuestions()),
                () -> assertEquals(LocalTime.of(19, 0), notification.defaultPreferredStudyTime()),
                () -> assertEquals(LocalTime.of(7, 0), notification.dailyPlanReminderTime()),
                () -> assertEquals(LocalTime.of(21, 0), notification.streakReminderTime()),
                () -> assertEquals(Duration.ofDays(30), idempotency.retention()));
    }

    @Test
    void loadsAllApprovedProfileResourcesWithoutSecretProperties() throws IOException {
        assertTrue(environment.matchesProfiles("test"));

        for (String resource : List.of(
                "application.yml",
                "application-local.yml",
                "application-test.yml",
                "application-prod.yml")) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
                assertNotNull(input, () -> "Thiếu profile: " + resource);
                String source = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                        .toLowerCase(Locale.ROOT);

                for (String forbiddenProperty : List.of(
                        "password:",
                        "secret:",
                        "api-key:",
                        "access-key:",
                        "private-key:",
                        "client-secret:",
                        "token:")) {
                    assertFalse(source.contains(forbiddenProperty),
                            () -> resource + " chứa thuộc tính nhạy cảm: " + forbiddenProperty);
                }
            }
        }
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
