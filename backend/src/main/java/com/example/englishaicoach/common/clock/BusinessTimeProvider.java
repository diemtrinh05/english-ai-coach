package com.example.englishaicoach.common.clock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Cung cấp thời gian nghiệp vụ từ một {@link Clock} có thể thay thế trong kiểm thử.
 * Chuỗi timezone truyền vào phương thức {@link #today(String)} phải lấy từ
 * {@code user_profiles.timezone}, không lấy từ timezone của máy chủ.
 */
public final class BusinessTimeProvider {

    private final Clock clock;

    public BusinessTimeProvider(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock không được null");
    }

    /**
     * Trả về thời điểm hiện tại do Clock được inject cung cấp.
     */
    public Instant now() {
        return clock.instant();
    }

    /**
     * Tính ngày local hiện tại theo IANA timezone lưu trong profile người dùng.
     */
    public LocalDate today(String userProfileTimezone) {
        Objects.requireNonNull(userProfileTimezone, "userProfileTimezone không được null");
        ZoneId zoneId = ZoneId.of(userProfileTimezone);
        return LocalDate.ofInstant(clock.instant(), zoneId);
    }
}
