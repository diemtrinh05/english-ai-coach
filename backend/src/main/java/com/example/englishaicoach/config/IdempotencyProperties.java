package com.example.englishaicoach.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Chính sách thời gian lưu idempotency record của approved baseline.
 * {@code retention} dùng {@link Duration}; record này không triển khai claim, replay
 * hoặc cleanup behavior của các task idempotency sau.
 */
@ConfigurationProperties("app.idempotency")
public record IdempotencyProperties(Duration retention) {
}
