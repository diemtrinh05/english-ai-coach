package com.example.englishaicoach.learning;

import com.example.englishaicoach.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_vocabulary_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_vocabulary_progress_user_vocabulary",
                columnNames = {"user_id", "vocabulary_id"}))
public class UserVocabularyProgress extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "vocabulary_id", nullable = false)
    private UUID vocabularyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LearningStatus status;

    @Column(name = "ease_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal easeFactor;

    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays;

    @Column(name = "repetitions", nullable = false)
    private Integer repetitions;

    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "incorrect_count", nullable = false)
    private Integer incorrectCount;

    @Column(name = "avg_response_time_ms")
    private Integer averageResponseTimeMs;

    @Column(name = "last_quality")
    private Short lastQuality;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected UserVocabularyProgress() {
    }

    public Long getVersion() {
        return version;
    }
}
