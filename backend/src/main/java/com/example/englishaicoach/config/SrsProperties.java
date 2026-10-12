package com.example.englishaicoach.config;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Các tham số typed của {@code sm2-ext-v1}; record này chỉ cung cấp đầu vào cấu hình,
 * không triển khai phép tính SRS.
 *
 * <p>Interval dùng đơn vị ngày, response time dùng {@link Duration}, quality dùng thang
 * 0..5, còn ease/time factor là hệ số không đơn vị. Các cặp minimum/maximum là biên clamp
 * của approved baseline; thay đổi default phải đi qua quy trình thay đổi baseline.</p>
 */
@ConfigurationProperties("app.srs")
public record SrsProperties(
        String algorithmVersion,
        BigDecimal initialEaseFactor,
        BigDecimal minimumEaseFactor,
        int minimumIntervalDays,
        int maximumIntervalDays,
        int firstSuccessIntervalDays,
        int secondSuccessIntervalDays,
        Duration responseTimeReference,
        Duration responseTimeFloor,
        BigDecimal minimumTimeFactor,
        BigDecimal maximumTimeFactor,
        int correctQualityThreshold,
        int masteredMinimumQuality,
        int masteredMinimumRepetitions,
        int masteredMinimumIntervalDays) {

    public SrsProperties {
        if (!"sm2-ext-v1".equals(algorithmVersion)
                || !positive(initialEaseFactor) || !positive(minimumEaseFactor)
                || initialEaseFactor.compareTo(minimumEaseFactor) < 0
                || minimumIntervalDays < 1 || maximumIntervalDays < minimumIntervalDays
                || firstSuccessIntervalDays < minimumIntervalDays
                || firstSuccessIntervalDays > maximumIntervalDays
                || secondSuccessIntervalDays < firstSuccessIntervalDays
                || secondSuccessIntervalDays > maximumIntervalDays
                || !positiveMillis(responseTimeReference) || !positiveMillis(responseTimeFloor)
                || !positive(minimumTimeFactor) || !positive(maximumTimeFactor)
                || minimumTimeFactor.compareTo(maximumTimeFactor) > 0
                || correctQualityThreshold != 3
                || masteredMinimumQuality < correctQualityThreshold || masteredMinimumQuality > 5
                || masteredMinimumRepetitions < 1
                || masteredMinimumIntervalDays < minimumIntervalDays
                || masteredMinimumIntervalDays > maximumIntervalDays) {
            throw new IllegalArgumentException("Cấu hình sm2-ext-v1 không hợp lệ");
        }
    }

    private static boolean positive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private static boolean positiveMillis(Duration value) {
        if (value == null || value.isNegative() || value.isZero()) {
            return false;
        }
        try {
            return value.toMillis() > 0 && value.equals(Duration.ofMillis(value.toMillis()));
        } catch (ArithmeticException exception) {
            return false;
        }
    }
}
