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
}
