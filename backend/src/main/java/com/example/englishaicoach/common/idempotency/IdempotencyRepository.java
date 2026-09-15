package com.example.englishaicoach.common.idempotency;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class IdempotencyRepository {

    private final JdbcTemplate jdbcTemplate;

    IdempotencyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Optional<IdempotencyRecord> findByEventId(UUID eventId) {
        return jdbcTemplate.query("""
                        SELECT event_id, user_id, endpoint, request_hash,
                               response_snapshot::text, response_status, created_at
                        FROM idempotency_keys
                        WHERE event_id = ?
                        """,
                (resultSet, rowNumber) -> new IdempotencyRecord(
                        resultSet.getObject("event_id", UUID.class),
                        resultSet.getObject("user_id", UUID.class),
                        resultSet.getString("endpoint"),
                        resultSet.getString("request_hash"),
                        resultSet.getString("response_snapshot"),
                        resultSet.getObject("response_status", Integer.class),
                        resultSet.getTimestamp("created_at").toInstant()),
                eventId).stream().findFirst();
    }

    boolean claim(UUID eventId, UUID userId, String endpoint, String requestHash) {
        int inserted = jdbcTemplate.update("""
                INSERT INTO idempotency_keys(event_id, user_id, endpoint, request_hash, created_at)
                VALUES (?, ?, ?, ?, now())
                ON CONFLICT (event_id) DO NOTHING
                """, eventId, userId, endpoint, requestHash);
        return inserted == 1;
    }

    void storeResponse(UUID eventId, int responseStatus, String responseSnapshot) {
        int updated = jdbcTemplate.update("""
                UPDATE idempotency_keys
                SET response_status = ?, response_snapshot = ?::jsonb
                WHERE event_id = ?
                """, responseStatus, responseSnapshot, eventId);
        if (updated != 1) {
            throw new IllegalStateException("Không thể lưu idempotency response cho eventId " + eventId);
        }
    }

    int deleteCreatedBefore(Instant cutoff) {
        return jdbcTemplate.update(
                "DELETE FROM idempotency_keys WHERE created_at < ?",
                Timestamp.from(cutoff));
    }
}
