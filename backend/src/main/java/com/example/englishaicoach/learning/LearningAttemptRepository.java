package com.example.englishaicoach.learning;

import jakarta.persistence.EntityManager;
import com.example.englishaicoach.config.SrsProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LearningAttemptRepository {
    private final JdbcTemplate jdbc;
    private final EntityManager entityManager;
    private final SrsProperties properties;

    public LearningAttemptRepository(JdbcTemplate jdbc, EntityManager entityManager, SrsProperties properties) {
        this.jdbc = jdbc;
        this.entityManager = entityManager;
        this.properties = properties;
    }

    public java.util.Optional<SessionState> lockOwnedSession(UUID sessionId, UUID userId) {
        // Khóa session để aggregate không mất cập nhật và completion không vượt attempt đang ghi.
        return jdbc.query("SELECT ended_at FROM learning_sessions WHERE id=? AND user_id=? FOR UPDATE",
                (rs, row) -> new SessionState(rs.getTimestamp(1) != null),
                sessionId, userId).stream().findFirst();
    }

    public boolean lockActiveVocabulary(UUID vocabularyId) {
        return !jdbc.query("SELECT id FROM vocabulary WHERE id=? AND is_active=true FOR SHARE",
                (rs, row) -> rs.getObject(1, UUID.class), vocabularyId).isEmpty();
    }

    public UserVocabularyProgress loadOrInitialize(UUID userId, UUID wordId, Instant now) {
        var existing = find(userId, wordId);
        if (existing != null) return existing;
        jdbc.update("""
                INSERT INTO user_vocabulary_progress
                (id,user_id,vocabulary_id,status,ease_factor,interval_days,repetitions,
                 correct_count,incorrect_count,version,created_at,updated_at)
                VALUES (?,?,?,'NEW',?,0,0,0,0,0,?,?)
                ON CONFLICT(user_id,vocabulary_id) DO NOTHING
                """, UUID.randomUUID(), userId, wordId, properties.initialEaseFactor(), Timestamp.from(now), Timestamp.from(now));
        return java.util.Objects.requireNonNull(find(userId, wordId));
    }

    private UserVocabularyProgress find(UUID userId, UUID wordId) {
        return entityManager.createQuery("SELECT p FROM UserVocabularyProgress p WHERE p.userId=:u AND p.vocabularyId=:v",
                        UserVocabularyProgress.class)
                .setParameter("u", userId).setParameter("v", wordId).getResultStream().findFirst().orElse(null);
    }

    public void record(UUID attemptId, UUID sessionId, UUID wordId, AttemptType type,
            boolean correct, int responseTime, int quality, Instant now) {
        jdbc.update("""
                INSERT INTO session_attempts
                (id,session_id,vocabulary_id,attempt_type,is_correct,response_time_ms,answer_quality,attempted_at)
                VALUES (?,?,?,?,?,?,?,?)
                """, attemptId, sessionId, wordId, type.name(), correct, responseTime, quality, Timestamp.from(now));
    }

    public int averageResponseTime(UUID userId, UUID wordId) {
        return jdbc.queryForObject("""
                SELECT AVG(a.response_time_ms::numeric) FROM session_attempts a
                JOIN learning_sessions s ON s.id=a.session_id
                WHERE s.user_id=? AND a.vocabulary_id=? AND a.response_time_ms IS NOT NULL
                """, BigDecimal.class, userId, wordId).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    public void updateSessionAggregates(UUID sessionId) {
        jdbc.update("""
                UPDATE learning_sessions SET words_studied_count=(
                    SELECT COUNT(DISTINCT vocabulary_id) FROM session_attempts WHERE session_id=?),
                    accuracy_percent=(SELECT ROUND(100.0*COUNT(*) FILTER (WHERE is_correct)/COUNT(*),2)
                        FROM session_attempts WHERE session_id=?) WHERE id=?
                """, sessionId, sessionId, sessionId);
    }

    public void flush() { entityManager.flush(); }

    public record SessionState(boolean closed) { }
}
