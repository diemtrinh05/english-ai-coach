package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.englishaicoach.config.SrsProperties;
import com.example.englishaicoach.learning.LearningStatus;
import com.example.englishaicoach.learning.srs.SrsCalculationInput;
import com.example.englishaicoach.learning.srs.SrsService;
import com.example.englishaicoach.learning.srs.SrsTransitionResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SrsBoundaryMatrixTests {

    private static final Instant NOW = Instant.parse("2026-10-13T23:59:59.123456Z");
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=test")
            .withUserConfiguration(PropertiesConfiguration.class);

    @ParameterizedTest
    @CsvSource({
            "0,1.70,false,0", "1,1.96,false,0", "2,2.18,false,0",
            "3,2.36,true,1", "4,2.50,true,1", "5,2.60,true,1"
    })
    void firstAcceptedAttemptQualityMatrix(int quality, String ease, boolean correct, int repetitions) {
        runner.run(context -> {
            SrsProperties properties = context.getBean(SrsProperties.class);
            SrsTransitionResult actual = new SrsService(properties).calculate(LearningStatus.NEW,
                    new SrsCalculationInput(properties.initialEaseFactor(), 0, 0, quality, 3000, NOW));
            assertResult(actual, LearningStatus.LEARNING, ease, repetitions, 1, correct);
        });
    }

    @Test
    void successiveQualityFourAttemptsReachReviewingThenMasteredOnFifthSuccess() {
        runner.run(context -> {
            SrsService service = new SrsService(context.getBean(SrsProperties.class));
            LearningStatus status = LearningStatus.NEW;
            BigDecimal ease = new BigDecimal("2.50");
            int interval = 0;
            for (int repetition = 1; repetition <= 5; repetition++) {
                SrsTransitionResult actual = service.calculate(status,
                        new SrsCalculationInput(ease, interval, repetition - 1, 4, 3000, NOW));
                int expectedInterval = switch (repetition) {
                    case 1 -> 1;
                    case 2 -> 6;
                    case 3 -> 15;
                    case 4 -> 38;
                    default -> 95;
                };
                LearningStatus expectedStatus = repetition == 1 ? LearningStatus.LEARNING
                        : repetition == 5 ? LearningStatus.MASTERED : LearningStatus.REVIEWING;
                assertResult(actual, expectedStatus, "2.50", repetition, expectedInterval, true);
                status = actual.status();
                ease = actual.calculation().newEaseFactor();
                interval = actual.calculation().newIntervalDays();
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
            "0,1.70", "1,1.96", "2,2.18"
    })
    void failedReviewRegressesAndResetsDespiteLongPriorInterval(int quality, String ease) {
        runner.run(context -> {
            SrsService service = new SrsService(context.getBean(SrsProperties.class));
            SrsCalculationInput input = input("2.50", 180, 9, quality, 0);
            assertResult(service.calculate(LearningStatus.REVIEWING, input),
                    LearningStatus.LEARNING, ease, 0, 1, false);
            assertResult(service.calculate(LearningStatus.MASTERED, input),
                    LearningStatus.LEARNING, ease, 0, 1, false);
        });
    }

    @Test
    void recoveryAfterFailureStartsAtFirstRepetitionAgain() {
        runner.run(context -> {
            SrsService service = new SrsService(context.getBean(SrsProperties.class));
            SrsTransitionResult failure = service.calculate(LearningStatus.REVIEWING,
                    input("2.50", 30, 5, 0, 3000));
            assertResult(failure, LearningStatus.LEARNING, "1.70", 0, 1, false);
            SrsTransitionResult firstRecovery = service.calculate(failure.status(),
                    new SrsCalculationInput(failure.calculation().newEaseFactor(),
                            failure.calculation().newIntervalDays(),
                            failure.calculation().newRepetitions(), 4, 3000, NOW));
            assertResult(firstRecovery, LearningStatus.LEARNING, "1.70", 1, 1, true);
            SrsTransitionResult secondRecovery = service.calculate(firstRecovery.status(),
                    new SrsCalculationInput(firstRecovery.calculation().newEaseFactor(),
                            firstRecovery.calculation().newIntervalDays(),
                            firstRecovery.calculation().newRepetitions(), 4, 3000, NOW));
            assertResult(secondRecovery, LearningStatus.REVIEWING, "1.70", 2, 6, true);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "0,22", "999,22", "1000,22", "3000,20", "3500,17",
            "10000,17", "2147483647,17"
    })
    void responseTimeFloorReferenceAndClampsAffectIntervalWithinBounds(int milliseconds, int interval) {
        runner.run(context -> assertResult(
                new SrsService(context.getBean(SrsProperties.class)).calculate(LearningStatus.REVIEWING,
                        input("2.50", 8, 2, 4, milliseconds)),
                LearningStatus.REVIEWING, "2.50", 3, interval, true));
    }

    @Test
    void intervalBoundsApplyAfterResponseTimeAndDoNotAlterFailureOneDay() {
        runner.run(context -> {
            SrsService service = new SrsService(context.getBean(SrsProperties.class));
            assertResult(service.calculate(LearningStatus.REVIEWING,
                            input("1.30", 0, 2, 3, 10000)),
                    LearningStatus.REVIEWING, "1.30", 3, 1, true);
            assertResult(service.calculate(LearningStatus.REVIEWING,
                            input("2.50", 180, 4, 5, 10000)),
                    LearningStatus.MASTERED, "2.60", 5, 180, true);
            assertResult(service.calculate(LearningStatus.REVIEWING,
                            input("2.50", 180, 4, 2, 0)),
                    LearningStatus.LEARNING, "2.18", 0, 1, false);
        });
    }

    @Test
    void masteredEntryRequiresQualityRepetitionsAndFinalIntervalSimultaneously() {
        runner.run(context -> {
            SrsService service = new SrsService(context.getBean(SrsProperties.class));
            assertResult(service.calculate(LearningStatus.REVIEWING,
                            input("2.50", 12, 4, 4, 3000)),
                    LearningStatus.MASTERED, "2.50", 5, 30, true);
            assertResult(service.calculate(LearningStatus.REVIEWING,
                            input("2.50", 13, 4, 3, 3000)),
                    LearningStatus.REVIEWING, "2.36", 5, 31, true);
            assertResult(service.calculate(LearningStatus.REVIEWING,
                            input("2.50", 12, 3, 4, 3000)),
                    LearningStatus.REVIEWING, "2.50", 4, 30, true);
            assertResult(service.calculate(LearningStatus.REVIEWING,
                            input("2.50", 11, 4, 4, 3000)),
                    LearningStatus.REVIEWING, "2.50", 5, 28, true);
            assertResult(service.calculate(LearningStatus.REVIEWING,
                            input("2.50", 12, 4, 4, 10000)),
                    LearningStatus.REVIEWING, "2.50", 5, 26, true);
        });
    }

    @Test
    void masteredSuccessAtQualityThreeRetainsStatusAndFailureRegresses() {
        runner.run(context -> {
            SrsService service = new SrsService(context.getBean(SrsProperties.class));
            assertResult(service.calculate(LearningStatus.MASTERED,
                            input("2.50", 30, 5, 3, 3000)),
                    LearningStatus.MASTERED, "2.36", 6, 71, true);
            assertResult(service.calculate(LearningStatus.MASTERED,
                            input("2.50", 30, 5, 2, 3000)),
                    LearningStatus.LEARNING, "2.18", 0, 1, false);
        });
    }

    private SrsCalculationInput input(String ease, int interval, int repetitions,
            int quality, int milliseconds) {
        return new SrsCalculationInput(new BigDecimal(ease), interval, repetitions,
                quality, milliseconds, NOW);
    }

    private void assertResult(SrsTransitionResult actual, LearningStatus status, String ease,
            int repetitions, int interval, boolean correct) {
        assertEquals(status, actual.status());
        assertEquals(0, new BigDecimal(ease).compareTo(actual.calculation().newEaseFactor()));
        assertEquals(repetitions, actual.calculation().newRepetitions());
        assertEquals(interval, actual.calculation().newIntervalDays());
        assertEquals(correct, actual.calculation().isCorrect());
        assertEquals(NOW.plus(interval, ChronoUnit.DAYS), actual.calculation().nextReviewAt());
        assertEquals("sm2-ext-v1", actual.calculation().algorithmVersion());
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SrsProperties.class)
    static class PropertiesConfiguration {
    }
}
