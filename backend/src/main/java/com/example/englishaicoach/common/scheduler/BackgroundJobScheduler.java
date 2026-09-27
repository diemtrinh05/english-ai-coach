package com.example.englishaicoach.common.scheduler;

import com.example.englishaicoach.common.clock.BusinessTimeProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Kích hoạt các worker nền qua Spring Scheduler. Nhịp quét là tham số vận hành,
 * không quyết định giờ local hoặc điều kiện nghiệp vụ của từng worker.
 */
@Component
public class BackgroundJobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundJobScheduler.class);
    private static final String NOTIFICATION_POLL_INTERVAL =
            "${app.scheduler.notification-poll-interval:PT1M}";
    private static final String DAILY_PLAN_POLL_INTERVAL =
            "${app.scheduler.daily-plan-poll-interval:PT15M}";
    private static final String IDEMPOTENCY_CLEANUP_INTERVAL =
            "${app.scheduler.idempotency-cleanup-interval:PT24H}";
    private static final String AI_USAGE_POLL_INTERVAL =
            "${app.scheduler.ai-usage-poll-interval:PT1H}";

    private final BusinessTimeProvider timeProvider;
    private final Map<BackgroundJobType, BackgroundJob> jobs;

    public BackgroundJobScheduler(BusinessTimeProvider timeProvider, List<BackgroundJob> workers) {
        this.timeProvider = Objects.requireNonNull(timeProvider, "timeProvider không được null");
        Objects.requireNonNull(workers, "workers không được null");
        this.jobs = new EnumMap<>(BackgroundJobType.class);
        for (BackgroundJob worker : workers) {
            Objects.requireNonNull(worker, "worker không được null");
            BackgroundJobType type = Objects.requireNonNull(worker.type(), "job type không được null");
            if (jobs.putIfAbsent(type, worker) != null) {
                throw new IllegalStateException("Trùng worker cho job type " + type);
            }
        }
    }

    @Scheduled(initialDelayString = NOTIFICATION_POLL_INTERVAL,
            fixedDelayString = NOTIFICATION_POLL_INTERVAL)
    public void pollNotifications() {
        dispatch(BackgroundJobType.NOTIFICATION);
    }

    @Scheduled(initialDelayString = DAILY_PLAN_POLL_INTERVAL,
            fixedDelayString = DAILY_PLAN_POLL_INTERVAL)
    public void pollDailyPlans() {
        dispatch(BackgroundJobType.DAILY_PLAN_PREGENERATION);
    }

    @Scheduled(initialDelayString = IDEMPOTENCY_CLEANUP_INTERVAL,
            fixedDelayString = IDEMPOTENCY_CLEANUP_INTERVAL)
    public void pollIdempotencyCleanup() {
        dispatch(BackgroundJobType.IDEMPOTENCY_CLEANUP);
    }

    @Scheduled(initialDelayString = AI_USAGE_POLL_INTERVAL,
            fixedDelayString = AI_USAGE_POLL_INTERVAL)
    public void pollAiUsageAggregation() {
        dispatch(BackgroundJobType.AI_USAGE_AGGREGATION);
    }

    private void dispatch(BackgroundJobType type) {
        BackgroundJob worker = jobs.get(type);
        if (worker == null) {
            return;
        }
        try {
            worker.run(timeProvider.now());
        } catch (RuntimeException exception) {
            LOGGER.error("background_job_failed jobType={} causeType={}",
                    type, exception.getClass().getSimpleName());
        }
    }
}
