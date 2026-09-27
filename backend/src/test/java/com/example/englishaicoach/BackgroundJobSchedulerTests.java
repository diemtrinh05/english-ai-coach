package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.englishaicoach.common.clock.BusinessTimeProvider;
import com.example.englishaicoach.common.idempotency.IdempotencyCleanupJob;
import com.example.englishaicoach.common.idempotency.IdempotencyService;
import com.example.englishaicoach.common.scheduler.BackgroundJob;
import com.example.englishaicoach.common.scheduler.BackgroundJobScheduler;
import com.example.englishaicoach.common.scheduler.BackgroundJobType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BackgroundJobSchedulerTests {

    private static final Instant FIXED_TIME = Instant.parse("2026-09-27T06:30:00Z");

    @Test
    void dispatchesEachRegisteredJobWithInjectedClock() {
        List<String> invocations = new ArrayList<>();
        BackgroundJobScheduler scheduler = scheduler(List.of(
                job(BackgroundJobType.NOTIFICATION, invocations),
                job(BackgroundJobType.DAILY_PLAN_PREGENERATION, invocations),
                job(BackgroundJobType.IDEMPOTENCY_CLEANUP, invocations),
                job(BackgroundJobType.AI_USAGE_AGGREGATION, invocations)));

        scheduler.pollNotifications();
        scheduler.pollDailyPlans();
        scheduler.pollIdempotencyCleanup();
        scheduler.pollAiUsageAggregation();

        assertEquals(List.of(
                "NOTIFICATION@" + FIXED_TIME,
                "DAILY_PLAN_PREGENERATION@" + FIXED_TIME,
                "IDEMPOTENCY_CLEANUP@" + FIXED_TIME,
                "AI_USAGE_AGGREGATION@" + FIXED_TIME), invocations);
    }

    @Test
    void missingFutureWorkersDoNotExecuteOtherJobTypes() {
        List<String> invocations = new ArrayList<>();
        BackgroundJobScheduler scheduler = scheduler(List.of(
                job(BackgroundJobType.IDEMPOTENCY_CLEANUP, invocations)));

        scheduler.pollNotifications();
        scheduler.pollDailyPlans();
        scheduler.pollAiUsageAggregation();
        scheduler.pollIdempotencyCleanup();

        assertEquals(List.of("IDEMPOTENCY_CLEANUP@" + FIXED_TIME), invocations);
    }

    @Test
    void rejectsDuplicateWorkersForOneJobType() {
        List<String> invocations = new ArrayList<>();
        assertThrows(IllegalStateException.class, () -> scheduler(List.of(
                job(BackgroundJobType.NOTIFICATION, invocations),
                job(BackgroundJobType.NOTIFICATION, invocations))));
    }

    @Test
    void oneWorkerFailureDoesNotPreventAnotherJobFromRunning() {
        List<String> invocations = new ArrayList<>();
        BackgroundJob failing = new BackgroundJob() {
            @Override
            public BackgroundJobType type() {
                return BackgroundJobType.NOTIFICATION;
            }

            @Override
            public void run(Instant now) {
                throw new IllegalStateException("test failure");
            }
        };
        BackgroundJobScheduler scheduler = scheduler(List.of(
                failing, job(BackgroundJobType.IDEMPOTENCY_CLEANUP, invocations)));

        scheduler.pollNotifications();
        scheduler.pollIdempotencyCleanup();

        assertEquals(List.of("IDEMPOTENCY_CLEANUP@" + FIXED_TIME), invocations);
    }

    @Test
    void cleanupWorkerDelegatesToExistingRetentionService() {
        IdempotencyService service = mock(IdempotencyService.class);
        IdempotencyCleanupJob cleanup = new IdempotencyCleanupJob(service);
        BackgroundJobScheduler scheduler = scheduler(List.of(cleanup));

        scheduler.pollIdempotencyCleanup();

        assertEquals(BackgroundJobType.IDEMPOTENCY_CLEANUP, cleanup.type());
        verify(service).deleteExpired(FIXED_TIME);
    }

    private BackgroundJobScheduler scheduler(List<BackgroundJob> jobs) {
        return new BackgroundJobScheduler(
                new BusinessTimeProvider(Clock.fixed(FIXED_TIME, ZoneOffset.UTC)), jobs);
    }

    private BackgroundJob job(BackgroundJobType type, List<String> invocations) {
        return new BackgroundJob() {
            @Override
            public BackgroundJobType type() {
                return type;
            }

            @Override
            public void run(Instant now) {
                invocations.add(type + "@" + now);
            }
        };
    }
}
