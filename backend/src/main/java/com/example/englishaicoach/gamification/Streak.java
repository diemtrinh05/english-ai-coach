package com.example.englishaicoach.gamification;

import com.example.englishaicoach.common.persistence.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "streaks",
        uniqueConstraints = @UniqueConstraint(
                name = "streaks_user_id_key",
                columnNames = "user_id"))
public class Streak extends UuidEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Streak() {
    }

    public Long getVersion() {
        return version;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UUID getUserId() { return userId; }
    public Integer getCurrentStreak() { return currentStreak; }
    public Integer getLongestStreak() { return longestStreak; }
    public LocalDate getLastActiveDate() { return lastActiveDate; }

    /** Ngày lùi hoặc đã xử lý không làm thay đổi state và version. */
    boolean activate(LocalDate date, Instant now) {
        if (lastActiveDate != null && !date.isAfter(lastActiveDate)) return false;
        currentStreak = lastActiveDate != null && date.equals(lastActiveDate.plusDays(1))
                ? Math.addExact(currentStreak, 1) : 1;
        longestStreak = Math.max(longestStreak, currentStreak);
        lastActiveDate = date;
        updatedAt = now;
        return true;
    }
}
