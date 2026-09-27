package com.example.englishaicoach.common.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public final class LocalRateLimitGate implements RateLimitGate {
    private final Clock clock;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong operations = new AtomicLong();

    public LocalRateLimitGate(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean allow(String route, String remoteAddress, int maximum, Duration window) {
        Instant now = clock.instant();
        String key = route + '\u0000' + remoteAddress;
        Window current = windows.compute(key, (ignored, prior) ->
                prior == null || !now.isBefore(prior.expiresAt())
                        ? new Window(1, now.plus(window))
                        : new Window(prior.count() + 1, prior.expiresAt()));
        if (operations.incrementAndGet() % 1024 == 0) {
            windows.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().expiresAt()));
        }
        return current.count() <= maximum;
    }

    private record Window(int count, Instant expiresAt) {
    }
}
