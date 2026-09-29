package com.example.englishaicoach.vocabulary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TopicRepository {

    private final JdbcTemplate jdbcTemplate;

    public TopicRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Topic> findActive(String search, UUID parentTopicId, int limit, long offset) {
        return jdbcTemplate.query("""
                SELECT id, name, description, icon_url, parent_topic_id, is_active
                FROM topics
                WHERE is_active = TRUE
                  AND (?::text IS NULL OR POSITION(LOWER(?) IN LOWER(name)) > 0)
                  AND (?::uuid IS NULL OR parent_topic_id = ?::uuid)
                ORDER BY LOWER(name), id
                LIMIT ? OFFSET ?
                """, (rs, row) -> map(rs), search, search, parentTopicId, parentTopicId, limit, offset);
    }

    public long countActive(String search, UUID parentTopicId) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM topics
                WHERE is_active = TRUE
                  AND (?::text IS NULL OR POSITION(LOWER(?) IN LOWER(name)) > 0)
                  AND (?::uuid IS NULL OR parent_topic_id = ?::uuid)
                """, Long.class, search, search, parentTopicId, parentTopicId);
        return count == null ? 0 : count;
    }

    public Optional<Topic> findActiveById(UUID id) {
        return jdbcTemplate.query("""
                SELECT id, name, description, icon_url, parent_topic_id, is_active
                FROM topics WHERE id = ? AND is_active = TRUE
                """, (rs, row) -> map(rs), id).stream().findFirst();
    }

    private Topic map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Topic(rs.getObject("id", UUID.class), rs.getString("name"),
                rs.getString("description"), rs.getString("icon_url"),
                rs.getObject("parent_topic_id", UUID.class), rs.getBoolean("is_active"));
    }
}
