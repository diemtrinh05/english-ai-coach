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
        return jdbc.query("""
                SELECT id,session_type,started_at,ended_at,words_studied_count,accuracy_percent
                FROM learning_sessions WHERE id=? AND user_id=?
                """, (rs, row) -> new LearningSessionResponse(
                        rs.getObject("id", UUID.class),
                        LearningSessionType.valueOf(rs.getString("session_type")),
                        rs.getTimestamp("started_at").toInstant(),
                        rs.getTimestamp("ended_at") == null ? null : rs.getTimestamp("ended_at").toInstant(),
                        rs.getInt("words_studied_count"),
                        rs.getBigDecimal("accuracy_percent")), sessionId, userId).stream().findFirst();
    }
}
