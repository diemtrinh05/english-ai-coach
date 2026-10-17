package com.example.englishaicoach.learning;

import com.example.englishaicoach.learning.dto.LearningSessionResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LearningSessionRepository {
    private final JdbcTemplate jdbc;

    public LearningSessionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean userExists(UUID userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id=?)", Boolean.class, userId));
    }

    public boolean lockUser(UUID userId) {
        return !jdbc.query("SELECT id FROM users WHERE id=? FOR KEY SHARE",
                (rs, row) -> rs.getObject("id", UUID.class), userId).isEmpty();
    }

    public void insert(UUID sessionId, UUID userId, LearningSessionType type, Instant startedAt) {
        jdbc.update("""
                INSERT INTO learning_sessions
                    (id,user_id,session_type,started_at,ended_at,words_studied_count,accuracy_percent)
                VALUES (?,?,?,?,NULL,0,NULL)
                """, sessionId, userId, type.name(), Timestamp.from(startedAt));
    }

    public Optional<LearningSessionResponse> findOwned(UUID sessionId, UUID userId) {
        return findOwned(sessionId, userId, false);
    }

    public Optional<LearningSessionResponse> lockOwned(UUID sessionId, UUID userId) {
        return findOwned(sessionId, userId, true);
    }

    public boolean hasAcceptedAttempt(UUID sessionId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM session_attempts WHERE session_id=?)", Boolean.class, sessionId));
    }

    public void complete(UUID sessionId, Instant endedAt) {
        jdbc.update("""
                UPDATE learning_sessions SET ended_at=?,words_studied_count=(
                    SELECT COUNT(DISTINCT vocabulary_id) FROM session_attempts WHERE session_id=?),
                    accuracy_percent=(SELECT ROUND(100.0*COUNT(*) FILTER (WHERE is_correct)/COUNT(*),2)
                        FROM session_attempts WHERE session_id=?) WHERE id=?
                """, Timestamp.from(endedAt), sessionId, sessionId, sessionId);
    }

    private Optional<LearningSessionResponse> findOwned(UUID sessionId, UUID userId, boolean lock) {
        return jdbc.query("""
                SELECT id,session_type,started_at,ended_at,words_studied_count,accuracy_percent
                FROM learning_sessions WHERE id=? AND user_id=?
                """ + (lock ? " FOR UPDATE" : ""), (rs, row) -> new LearningSessionResponse(
                        rs.getObject("id", UUID.class),
                        LearningSessionType.valueOf(rs.getString("session_type")),
                        rs.getTimestamp("started_at").toInstant(),
                        rs.getTimestamp("ended_at") == null ? null : rs.getTimestamp("ended_at").toInstant(),
                        rs.getInt("words_studied_count"),
                        rs.getBigDecimal("accuracy_percent")), sessionId, userId).stream().findFirst();
    }
}
