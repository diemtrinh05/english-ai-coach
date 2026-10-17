package com.example.englishaicoach.progress;

import com.example.englishaicoach.learning.LearningStatus;
import com.example.englishaicoach.learning.dto.VocabularyProgressResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VocabularyProgressRepository {
    private final JdbcTemplate jdbc;

    public VocabularyProgressRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean userExists(UUID userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id=?)", Boolean.class, userId));
    }

    public boolean activeVocabularyExists(UUID vocabularyId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM vocabulary WHERE id=? AND is_active=TRUE)", Boolean.class, vocabularyId));
    }

    public Optional<VocabularyProgressResponse> find(UUID userId, UUID vocabularyId) {
        return jdbc.query("""
                SELECT vocabulary_id,status,ease_factor,interval_days,repetitions,
                       next_review_at,correct_count,incorrect_count
                FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?
                """, (rs, row) -> {
                    var nextReviewAt = rs.getTimestamp("next_review_at");
                    return new VocabularyProgressResponse(rs.getObject("vocabulary_id", UUID.class),
                            LearningStatus.valueOf(rs.getString("status")), rs.getBigDecimal("ease_factor"),
                            rs.getInt("interval_days"), rs.getInt("repetitions"),
                            nextReviewAt == null ? null : nextReviewAt.toInstant(),
                            rs.getInt("correct_count"), rs.getInt("incorrect_count"));
                }, userId, vocabularyId).stream().findFirst();
    }
}
