package com.example.englishaicoach.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Aggregate lưu trạng thái canonical; chưa chứa orchestration start/answer của API. */
@Entity
@Table(name = "user_level_assessments")
public class UserLevelAssessment {
    // UUID được cấp trước selector để cùng logical operation giữ nguyên đầu vào deterministic.
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 30)
    private AssessmentType assessmentType;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssessmentStatus status;
    @Column(name = "current_cefr_level_id", nullable = false)
    private UUID currentCefrLevelId;
    @Column(name = "final_cefr_level_id")
    private UUID finalCefrLevelId;
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;
    @Column(name = "questions_answered", nullable = false)
    private int questionsAnswered;
    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;
    @Column(name = "block_questions", nullable = false)
    private int blockQuestions;
    @Column(name = "block_correct", nullable = false)
    private int blockCorrect;
    @Column(name = "stable_block_count", nullable = false)
    private int stableBlockCount;
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserLevelAssessment() { }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public AssessmentType getAssessmentType() { return assessmentType; }
    public AssessmentStatus getStatus() { return status; }
    public UUID getCurrentCefrLevelId() { return currentCefrLevelId; }
    public UUID getFinalCefrLevelId() { return finalCefrLevelId; }
    public BigDecimal getScore() { return score; }
    public int getQuestionsAnswered() { return questionsAnswered; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getBlockQuestions() { return blockQuestions; }
    public int getBlockCorrect() { return blockCorrect; }
    public int getStableBlockCount() { return stableBlockCount; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
