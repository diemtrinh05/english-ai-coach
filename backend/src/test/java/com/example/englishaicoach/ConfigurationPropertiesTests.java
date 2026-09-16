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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ConfigurationPropertiesTests {

    private static final Pattern SENSITIVE_PROPERTY = Pattern.compile(
            "(?im)^\\s*(?:password|secret|api-key|access-key|private-key|client-secret|token):\\s*(?<value>[^#\\r\\n]+)");

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=test")
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void bindsCanonicalAssessmentAndSrsDefaults() {
        contextRunner.run(context -> {
            AssessmentProperties assessment = context.getBean(AssessmentProperties.class);
            SrsProperties srs = context.getBean(SrsProperties.class);

            assertFalse(context.containsBean("dataSource"));
            assertFalse(context.containsBean("flyway"));
            assertFalse(context.containsBean("entityManagerFactory"));

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
        });
    }

    @Test
    void bindsCanonicalPersonalizationDefaults() {
        contextRunner.run(context -> {
            PersonalizationProperties personalization = context.getBean(PersonalizationProperties.class);
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
        });
    }

    @Test
    void bindsCanonicalGamificationNotificationAndIdempotencyDefaults() {
        contextRunner.run(context -> {
            GamificationProperties gamification = context.getBean(GamificationProperties.class);
            NotificationProperties notification = context.getBean(NotificationProperties.class);
            IdempotencyProperties idempotency = context.getBean(IdempotencyProperties.class);

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
        });
    }

    @Test
    void loadsAllApprovedProfileResourcesWithoutLiteralSecrets() throws IOException {
        contextRunner.run(context -> {
            assertTrue(context.getEnvironment().matchesProfiles("test"));

            for (String resource : List.of(
                    "application.yml",
                    "application-local.yml",
                    "application-test.yml",
                    "application-prod.yml")) {
                try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
                    assertNotNull(input, () -> "Thiếu profile: " + resource);
                    String source = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                    Matcher matcher = SENSITIVE_PROPERTY.matcher(source);
                    while (matcher.find()) {
                        String value = matcher.group("value").trim();
                        assertTrue(value.matches("\\$\\{[A-Z][A-Z0-9_]*}"),
                                () -> resource + " chứa giá trị nhạy cảm không lấy từ environment: " + value);
                    }
                }
            }
        });
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties({
            AssessmentProperties.class,
            SrsProperties.class,
            PersonalizationProperties.class,
            GamificationProperties.class,
            NotificationProperties.class,
            IdempotencyProperties.class
    })
    static class PropertiesConfiguration {
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
