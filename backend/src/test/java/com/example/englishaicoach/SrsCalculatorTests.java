package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.englishaicoach.config.SrsProperties;
import com.example.englishaicoach.learning.srs.SrsCalculationInput;
import com.example.englishaicoach.learning.srs.SrsCalculationResult;
import com.example.englishaicoach.learning.srs.SrsCalculator;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SrsCalculatorTests {

    private static final Instant NOW = Instant.parse("2026-10-12T23:59:59.123456Z");
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=test")
            .withUserConfiguration(PropertiesConfiguration.class);

    @ParameterizedTest
    @CsvSource({"0,1.70,false", "1,1.96,false", "2,2.18,false", "3,2.36,true",
            "4,2.50,true", "5,2.60,true"})
    void derivesExactQualityAdjustmentAndCorrectness(int quality, String ease, boolean correct) {
        runner.run(context -> {
            SrsCalculator calculator = new SrsCalculator(context.getBean(SrsProperties.class));
            SrsCalculationInput input = calculator.initialInput(quality, 3000, NOW);
            SrsCalculationResult result = calculator.calculate(input);
            assertEquals(0, new BigDecimal(ease).compareTo(result.newEaseFactor()));
            assertEquals(correct, result.isCorrect());
            assertEquals(correct ? 1 : 0, result.newRepetitions());
            assertEquals(1, result.newIntervalDays());
            assertEquals(NOW.plusSeconds(86400), result.nextReviewAt());
            assertEquals("sm2-ext-v1", result.algorithmVersion());
            assertEquals(0, new BigDecimal("2.50").compareTo(input.oldEaseFactor()));
            assertEquals(0, input.oldIntervalDays());
            assertEquals(0, input.oldRepetitions());
            assertEquals(result, calculator.calculate(input));
        });
    }

    @Test
    void reproducesApprovedWorkedExampleAndPositiveHalfRounding() {
        assertCalculation("2.50", 6, 2, 4, 2500, "2.50", 17, 3);
    }

    @Test
    void roundsBaseBeforeTimeExtension() {
        assertCalculation("2.50", 3, 2, 4, 10000, "2.50", 7, 3);
    }

    @Test
    void usesNewEaseFactorForLaterRepetition() {
        assertCalculation("2.50", 6, 2, 5, 3000, "2.60", 16, 3);
    }

    @ParameterizedTest
    @CsvSource({"0,7", "1000,7", "3000,6", "3500,5", "2147483647,5"})
    void secondSuccessUsesBoundedResponseTime(int milliseconds, int expectedDays) {
        assertCalculation("2.50", 180, 1, 4, milliseconds, "2.50", expectedDays, 2);
    }

    @ParameterizedTest
    @CsvSource({"0", "2", "2147483647"})
    void failureResetsRepetitionAndOneDayWithoutTimeExtension(int milliseconds) {
        assertCalculation("1.30", 180, 20, 0, milliseconds, "1.30", 1, 0);
    }

    @Test
    void clampsFinalIntervalWithoutOverflowingBeforeClamp() {
        assertCalculation("1E+200", Integer.MAX_VALUE, 8, 5, 0,
                new BigDecimal("1E+200").add(new BigDecimal("0.10")).toPlainString(), 180, 9);
        assertCalculation("1.30", 0, 2, 3, 3000, "1.30", 1, 3);
    }

    @Test
    void timeAndStateAreExplicitAndNoInputMutationOccurs() {
        runner.run(context -> {
            SrsCalculator calculator = new SrsCalculator(context.getBean(SrsProperties.class));
            SrsCalculationInput input = new SrsCalculationInput(new BigDecimal("2.50"), 6, 2, 4, 2500, NOW);
            SrsCalculationResult result = calculator.calculate(input);
            assertEquals(NOW.plusSeconds(17L * 86400), result.nextReviewAt());
            assertEquals(6, input.oldIntervalDays());
            assertEquals(2, input.oldRepetitions());
            assertEquals(NOW, input.now());
        });
    }

    @Test
    void rejectsInvalidInputAndRepetitionOverflowWithoutSilentClamp() {
        assertThrows(IllegalArgumentException.class, () -> input("0", 0, 0, 4, 0));
        assertThrows(IllegalArgumentException.class, () -> input("2.5", -1, 0, 4, 0));
        assertThrows(IllegalArgumentException.class, () -> input("2.5", 0, -1, 4, 0));
        assertThrows(IllegalArgumentException.class, () -> input("2.5", 0, 0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> input("2.5", 0, 0, 6, 0));
        assertThrows(IllegalArgumentException.class, () -> input("2.5", 0, 0, 4, -1));
        assertThrows(NullPointerException.class, () -> new SrsCalculationInput(null, 0, 0, 4, 0, NOW));
        assertThrows(NullPointerException.class, () -> new SrsCalculationInput(BigDecimal.ONE, 0, 0, 4, 0, null));
        runner.run(context -> {
            SrsCalculator calculator = new SrsCalculator(context.getBean(SrsProperties.class));
            assertThrows(ArithmeticException.class,
                    () -> calculator.calculate(input("2.5", 6, Integer.MAX_VALUE, 4, 0)));
        });
    }

    private void assertCalculation(String ease, int interval, int repetitions, int quality,
            int milliseconds, String expectedEase, int expectedInterval, int expectedRepetitions) {
        runner.run(context -> {
            SrsCalculationResult result = new SrsCalculator(context.getBean(SrsProperties.class))
                    .calculate(input(ease, interval, repetitions, quality, milliseconds));
            assertEquals(0, new BigDecimal(expectedEase).compareTo(result.newEaseFactor()));
            assertEquals(expectedInterval, result.newIntervalDays());
            assertEquals(expectedRepetitions, result.newRepetitions());
            assertEquals(NOW.plusSeconds(expectedInterval * 86400L), result.nextReviewAt());
        });
    }

    private SrsCalculationInput input(String ease, int interval, int repetitions, int quality, int milliseconds) {
        return new SrsCalculationInput(new BigDecimal(ease), interval, repetitions, quality, milliseconds, NOW);
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SrsProperties.class)
    static class PropertiesConfiguration {
    }
}
