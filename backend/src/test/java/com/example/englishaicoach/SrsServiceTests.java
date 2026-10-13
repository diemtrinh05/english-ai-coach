package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.englishaicoach.config.SrsProperties;
import com.example.englishaicoach.learning.LearningStatus;
import com.example.englishaicoach.learning.srs.SrsCalculationInput;
import com.example.englishaicoach.learning.srs.SrsService;
import com.example.englishaicoach.learning.srs.SrsTransitionResult;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SrsServiceTests {

    private static final Instant NOW = Instant.parse("2026-10-13T00:00:00Z");
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=test")
            .withUserConfiguration(PropertiesConfiguration.class);

    @ParameterizedTest
    @CsvSource({"0,false", "1,false", "2,false", "3,true", "4,true", "5,true"})
    void firstAcceptedAttemptAlwaysLeavesNew(int quality, boolean correct) {
        runner.run(context -> {
            SrsProperties properties = context.getBean(SrsProperties.class);
            SrsCalculationInput input = new SrsCalculationInput(properties.initialEaseFactor(),
                    0, 0, quality, 3000, NOW);
            SrsTransitionResult result = new SrsService(properties).calculate(LearningStatus.NEW, input);
            assertEquals(LearningStatus.LEARNING, result.status());
            assertEquals(correct, result.calculation().isCorrect());
            assertEquals(correct ? 1 : 0, result.calculation().newRepetitions());
            assertEquals(1, result.calculation().newIntervalDays());
            assertEquals(NOW.plusSeconds(86400), result.calculation().nextReviewAt());
        });
    }

    @ParameterizedTest
    @CsvSource({"0,LEARNING,false,0", "1,LEARNING,false,0", "2,LEARNING,false,0",
            "3,REVIEWING,true,2", "4,REVIEWING,true,2", "5,REVIEWING,true,2"})
    void learningPromotesOnlyAfterSecondSuccessfulRepetition(int quality,
            LearningStatus expected, boolean correct, int repetitions) {
        assertTransition(LearningStatus.LEARNING, input("2.50", 1, 1, quality),
                expected, correct, repetitions);
    }

    @ParameterizedTest
    @CsvSource({"3", "4", "5"})
    void learningStaysLearningUntilTwoSuccesses(int quality) {
        assertTransition(LearningStatus.LEARNING, input("2.50", 0, 0, quality),
                LearningStatus.LEARNING, true, 1);
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "2"})
    void reviewingFailureRegressesToLearning(int quality) {
        assertTransition(LearningStatus.REVIEWING, input("2.50", 30, 6, quality),
                LearningStatus.LEARNING, false, 0);
    }

    @Test
    void reviewingMasteryNeedsEntryQualityRepetitionsAndIntervalTogether() {
        assertTransition(LearningStatus.REVIEWING, input("2.50", 12, 4, 4),
                LearningStatus.MASTERED, true, 5);
        assertTransition(LearningStatus.REVIEWING, input("2.50", 13, 4, 3),
                LearningStatus.REVIEWING, true, 5);
        assertTransition(LearningStatus.REVIEWING, input("2.50", 12, 3, 4),
                LearningStatus.REVIEWING, true, 4);
        assertTransition(LearningStatus.REVIEWING, input("2.50", 11, 4, 4),
                LearningStatus.REVIEWING, true, 5);
        assertTransition(LearningStatus.REVIEWING,
                new SrsCalculationInput(new BigDecimal("2.50"), 12, 4, 4, 10000, NOW),
                LearningStatus.REVIEWING, true, 5);
    }

    @ParameterizedTest
    @CsvSource({"0,LEARNING,false,0", "1,LEARNING,false,0", "2,LEARNING,false,0",
            "3,MASTERED,true,7", "4,MASTERED,true,7", "5,MASTERED,true,7"})
    void masteredStaysOnSuccessAndRegressesOnFailure(int quality,
            LearningStatus expected, boolean correct, int repetitions) {
        assertTransition(LearningStatus.MASTERED, input("2.50", 30, 6, quality),
                expected, correct, repetitions);
    }

    @Test
    void rejectsMissingOldStatusOrInputWithoutChangingInput() {
        runner.run(context -> {
            SrsService service = new SrsService(context.getBean(SrsProperties.class));
            SrsCalculationInput input = input("2.50", 12, 4, 4);
            assertThrows(NullPointerException.class, () -> service.calculate(null, input));
            assertThrows(NullPointerException.class, () -> service.calculate(LearningStatus.REVIEWING, null));
            SrsTransitionResult first = service.calculate(LearningStatus.REVIEWING, input);
            assertEquals(first, service.calculate(LearningStatus.REVIEWING, input));
            assertEquals(12, input.oldIntervalDays());
            assertEquals(4, input.oldRepetitions());
            assertEquals(NOW, input.now());
        });
    }

    private void assertTransition(LearningStatus previous, SrsCalculationInput input,
            LearningStatus expected, boolean correct, int repetitions) {
        runner.run(context -> {
            SrsTransitionResult result = new SrsService(context.getBean(SrsProperties.class))
                    .calculate(previous, input);
            assertEquals(expected, result.status());
            assertEquals(correct, result.calculation().isCorrect());
            assertEquals(repetitions, result.calculation().newRepetitions());
            assertEquals("sm2-ext-v1", result.calculation().algorithmVersion());
            assertEquals(NOW.plusSeconds(result.calculation().newIntervalDays() * 86400L),
                    result.calculation().nextReviewAt());
        });
    }

    private SrsCalculationInput input(String ease, int interval, int repetitions, int quality) {
        return new SrsCalculationInput(new BigDecimal(ease), interval, repetitions, quality, 3000, NOW);
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SrsProperties.class)
    static class PropertiesConfiguration {
    }
}
