package com.example.englishaicoach.gamification;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class XpLogRepository {
    private final JdbcTemplate jdbc;

    public XpLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertIfAbsent(XpLog log) {
        jdbc.update("""
                INSERT INTO xp_logs
                    (id,user_id,source_event_id,xp_amount,reason,reference_type,reference_id,created_at)
                VALUES (?,?,?,?,?,?,?,?)
                ON CONFLICT (user_id,reason,source_event_id) DO NOTHING
                """, log.id(), log.userId(), log.sourceEventId(), log.xpAmount(), log.reason(),
                log.referenceType(), log.referenceId(), Timestamp.from(log.createdAt()));
    }

    public Optional<XpLog> findSource(XpSource source) {
        return jdbc.query("""
                SELECT id,user_id,source_event_id,xp_amount,reason,reference_type,reference_id,created_at
                FROM xp_logs WHERE user_id=? AND reason=? AND source_event_id=?
                """, (rs, row) -> new XpLog(rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class),
                        rs.getObject("source_event_id", UUID.class), rs.getInt("xp_amount"), rs.getString("reason"),
                        rs.getString("reference_type"), rs.getObject("reference_id", UUID.class),
                        rs.getTimestamp("created_at").toInstant()), source.userId(), source.reason().name(),
                source.sourceEventId()).stream().findFirst();
    }

    public long totalXp(UUID userId) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(xp_amount),0) FROM xp_logs WHERE user_id=?",
                Long.class, userId);
    }
}
