package com.example.englishaicoach.common.scheduler;

/**
 * Các nhóm công việc nền được baseline V1 cho phép.
 */
public enum BackgroundJobType {
    NOTIFICATION,
    DAILY_PLAN_PREGENERATION,
    IDEMPOTENCY_CLEANUP,
    AI_USAGE_AGGREGATION
}
