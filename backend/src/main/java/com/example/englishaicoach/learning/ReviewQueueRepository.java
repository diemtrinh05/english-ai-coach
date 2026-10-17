package com.example.englishaicoach.learning;

import com.example.englishaicoach.learning.dto.ReviewQueueResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewQueueRepository {
    // EXISTS giữ mỗi vocabulary đúng một dòng khi thuộc nhiều topic.
    static final String QUERY = """
            SELECT p.vocabulary_id, v.word, p.status, p.next_review_at
            FROM user_vocabulary_progress p
            JOIN vocabulary v ON v.id=p.vocabulary_id AND v.is_active=TRUE
            WHERE p.user_id=? AND p.next_review_at<=?
            """;
    static final String TOPIC_FILTER = """
            AND EXISTS (SELECT 1 FROM vocabulary_topics vt
              JOIN topics t ON t.id=vt.topic_id AND t.is_active=TRUE
              WHERE vt.vocabulary_id=p.vocabulary_id AND vt.topic_id=?)
            """;
    static final String ORDER_LIMIT = " ORDER BY p.next_review_at ASC,p.vocabulary_id ASC LIMIT ?";
    private final JdbcTemplate jdbc;

    public ReviewQueueRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean userExists(UUID userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id=?)", Boolean.class, userId));
    }

    public List<ReviewQueueResponse.Item> findDue(UUID userId, Instant now, UUID topicId, int limit) {
        Object[] parameters = topicId == null
                ? new Object[]{userId, Timestamp.from(now), limit}
                : new Object[]{userId, Timestamp.from(now), topicId, limit};
        return jdbc.query(QUERY + (topicId == null ? "" : TOPIC_FILTER) + ORDER_LIMIT,
                (rs, row) -> new ReviewQueueResponse.Item(rs.getObject("vocabulary_id", UUID.class),
                        rs.getString("word"), rs.getString("status"),
                        rs.getTimestamp("next_review_at").toInstant()), parameters);
    }
}
