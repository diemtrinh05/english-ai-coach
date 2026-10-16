package com.example.englishaicoach.gamification;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StreakRepository {
    private final EntityManager entities;
    private final JdbcTemplate jdbc;

    public StreakRepository(EntityManager entities, JdbcTemplate jdbc) {
        this.entities = entities;
        this.jdbc = jdbc;
    }

    public Optional<Streak> findByUserId(UUID userId) {
        return entities.createQuery("SELECT s FROM Streak s WHERE s.userId=:userId", Streak.class)
                .setParameter("userId", userId).getResultStream().findFirst();
    }

    public Optional<String> findProfileTimezone(UUID userId) {
        return jdbc.query("SELECT timezone FROM user_profiles WHERE user_id=?",
                (rs, row) -> rs.getString("timezone"), userId).stream().findFirst();
    }

    public void initializeIfAbsent(UUID userId, Instant now) {
        // Không bắt unique violation rồi tiếp tục trong PostgreSQL transaction đã lỗi.
        jdbc.update("""
                INSERT INTO streaks(id,user_id,current_streak,longest_streak,last_active_date,version,updated_at)
                VALUES (?,?,0,0,NULL,0,?) ON CONFLICT (user_id) DO NOTHING
                """, UUID.randomUUID(), userId, Timestamp.from(now));
    }

    public void flush() { entities.flush(); }
}
