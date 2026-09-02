package com.example.englishaicoach.config;

import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Các mốc giờ local mặc định cho notification theo approved baseline.
 * {@link LocalTime} cố ý không mang UTC offset; task scheduling sau này phải diễn giải
 * các giá trị này theo timezone của user thay vì timezone máy chủ.
 */
@ConfigurationProperties("app.notification")
public record NotificationProperties(
        LocalTime defaultPreferredStudyTime,
        LocalTime dailyPlanReminderTime,
        LocalTime streakReminderTime) {
}
