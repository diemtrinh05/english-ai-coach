package com.example.englishaicoach.gamification;

import com.example.englishaicoach.config.GamificationProperties;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {
    private final XpLogRepository logs;
    private final GamificationProperties properties;
    private final Clock clock;

    public GamificationService(XpLogRepository logs, GamificationProperties properties, Clock clock) {
        this.logs = logs;
        this.properties = properties;
        this.clock = clock;
    }

    /** Ledger phải commit/rollback cùng mutation nguồn, không mở transaction độc lập. */
    @Transactional(propagation = Propagation.MANDATORY)
    public XpLog award(XpSource source) {
        Objects.requireNonNull(source, "source");
        int amount = source.reason().amount(properties);
        logs.insertIfAbsent(new XpLog(UUID.randomUUID(), source.userId(), source.sourceEventId(), amount,
                source.reason().name(), source.reason().referenceType(), source.referenceId(), clock.instant()));
        XpLog stored = logs.findSource(source).orElseThrow(() ->
                new IllegalStateException("Không tìm thấy ledger sau khi ghi nguồn XP."));
        if (stored.xpAmount() != amount
                || !Objects.equals(stored.referenceType(), source.reason().referenceType())
                || !Objects.equals(stored.referenceId(), source.referenceId())) {
            throw new IllegalStateException("Nguồn XP đã tồn tại với metadata không nhất quán.");
        }
        return stored;
    }

    @Transactional(readOnly = true)
    public long totalXp(UUID userId) {
        return logs.totalXp(Objects.requireNonNull(userId, "userId"));
    }
}
