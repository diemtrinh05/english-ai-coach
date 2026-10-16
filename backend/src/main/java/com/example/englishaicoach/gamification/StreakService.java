package com.example.englishaicoach.gamification;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StreakService {
    private final StreakRepository streaks;
    private final GamificationService gamification;
    private final Clock clock;

    public StreakService(StreakRepository streaks, GamificationService gamification, Clock clock) {
        this.streaks = streaks;
        this.gamification = gamification;
        this.clock = clock;
    }

    /** True khi state chuyển sang ngày mới; caller sở hữu accepted activity và transaction. */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean recordAcceptedActivity(UUID userId, StreakActivity activity) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(activity, "activity");
        if (activity == StreakActivity.ASSESSMENT) return false;
        String timezone = streaks.findProfileTimezone(userId).orElseThrow(() ->
                new IllegalStateException("Thiếu timezone profile cho activity đã chấp nhận."));
        if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
            throw new IllegalStateException("Timezone profile không hợp lệ.");
        }
        var now = clock.instant();
        LocalDate date = LocalDate.ofInstant(now, ZoneId.of(timezone));
        Streak streak = streaks.findByUserId(userId).orElseGet(() -> {
            streaks.initializeIfAbsent(userId, now);
            return streaks.findByUserId(userId).orElseThrow(() ->
                    new IllegalStateException("Không tìm thấy streak sau khởi tạo."));
        });
        if (!streak.activate(date, now)) return false;
        // Flush @Version trước award; conflict rollback cả mutation nguồn và ledger.
        streaks.flush();
        gamification.award(XpSource.streak(userId, streak.getId(), date));
        return true;
    }
}
