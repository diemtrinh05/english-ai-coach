package com.example.englishaicoach.learning.srs;

import com.example.englishaicoach.config.SrsProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Tính sm2-ext-v1 một lần tại backend, không I/O, mutation hoặc clock ngầm. */
public final class SrsCalculator {

    private final SrsProperties properties;

    public SrsCalculator(SrsProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public SrsCalculationInput initialInput(int answerQuality, int responseTimeMs, Instant now) {
        return new SrsCalculationInput(properties.initialEaseFactor(), 0, 0,
                answerQuality, responseTimeMs, now);
    }

    public SrsCalculationResult calculate(SrsCalculationInput input) {
        Objects.requireNonNull(input, "input");
        BigDecimal distance = BigDecimal.valueOf(5L - input.answerQuality());
        BigDecimal adjustment = new BigDecimal("0.10").subtract(distance.multiply(
                new BigDecimal("0.08").add(distance.multiply(new BigDecimal("0.02")))));
        BigDecimal newEaseFactor = input.oldEaseFactor().add(adjustment)
                .max(properties.minimumEaseFactor());
        boolean isCorrect = input.answerQuality() >= properties.correctQualityThreshold();
        int repetitions = isCorrect ? Math.addExact(input.oldRepetitions(), 1) : 0;
        int interval = properties.minimumIntervalDays();
        if (isCorrect) {
            BigDecimal baseInterval = switch (repetitions) {
                case 1 -> BigDecimal.valueOf(properties.firstSuccessIntervalDays());
                case 2 -> BigDecimal.valueOf(properties.secondSuccessIntervalDays());
                default -> BigDecimal.valueOf(input.oldIntervalDays()).multiply(newEaseFactor)
                        .setScale(0, RoundingMode.HALF_UP);
            };
            interval = extendedInterval(baseInterval, input.responseTimeMs());
        }
        return new SrsCalculationResult(newEaseFactor, interval, repetitions,
                input.now().plus(interval, ChronoUnit.DAYS), isCorrect, properties.algorithmVersion());
    }

    private int extendedInterval(BigDecimal baseInterval, int responseTimeMs) {
        BigDecimal numerator = BigDecimal.valueOf(properties.responseTimeReference().toMillis());
        BigDecimal denominator = BigDecimal.valueOf(Math.max(
                (long) responseTimeMs, properties.responseTimeFloor().toMillis()));
        // So sánh nhân chéo và chỉ chia tại bước round để không làm tròn time_factor sớm.
        if (numerator.compareTo(properties.minimumTimeFactor().multiply(denominator)) < 0) {
            numerator = properties.minimumTimeFactor();
            denominator = BigDecimal.ONE;
        } else if (numerator.compareTo(properties.maximumTimeFactor().multiply(denominator)) > 0) {
            numerator = properties.maximumTimeFactor();
            denominator = BigDecimal.ONE;
        }
        BigDecimal interval = baseInterval.multiply(numerator).divide(denominator, 0, RoundingMode.HALF_UP);
        return interval.max(BigDecimal.valueOf(properties.minimumIntervalDays()))
                .min(BigDecimal.valueOf(properties.maximumIntervalDays())).intValueExact();
    }
}
