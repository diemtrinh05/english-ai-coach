package com.example.englishaicoach.common.idempotency;

import com.example.englishaicoach.common.scheduler.BackgroundJob;
import com.example.englishaicoach.common.scheduler.BackgroundJobType;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Dùng chính sách retention đã được phê duyệt của IdempotencyService.
 */
@Component
public class IdempotencyCleanupJob implements BackgroundJob {

    private final IdempotencyService service;

    public IdempotencyCleanupJob(IdempotencyService service) {
        this.service = service;
    }

    @Override
    public BackgroundJobType type() {
        return BackgroundJobType.IDEMPOTENCY_CLEANUP;
    }

    @Override
    public void run(Instant now) {
        service.deleteExpired(now);
    }
}
