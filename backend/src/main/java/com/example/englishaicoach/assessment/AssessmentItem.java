package com.example.englishaicoach.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Snapshot độc lập catalog, gồm đáp án nội bộ; tuyệt đối không dùng entity làm REST response. */
@Entity
@Table(name = "assessment_items", uniqueConstraints = {
        @UniqueConstraint(name = "uq_assessment_items_sequence", columnNames = {"assessment_id", "sequence_no"}),
        @UniqueConstraint(name = "uq_assessment_items_vocabulary", columnNames = {"assessment_id", "vocabulary_id"})})
public class AssessmentItem {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "assessment_id", nullable = false, updatable = false)
    private UUID assessmentId;
    @Column(name = "sequence_no", nullable = false, updatable = false)
    private int sequenceNo;
    @Column(name = "vocabulary_id", nullable = false, updatable = false)
    private UUID vocabularyId;
    @Column(name = "cefr_level_id", nullable = false, updatable = false)
    private UUID cefrLevelId;
    @Column(name = "question_text", nullable = false, updatable = false, columnDefinition = "text")
    private String questionText;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json", nullable = false, updatable = false, columnDefinition = "jsonb")
    private List<String> options;
    @Column(name = "correct_answer", nullable = false, updatable = false, columnDefinition = "text")
    private String correctAnswer;
    @Column(name = "selected_answer", columnDefinition = "text")
    private String selectedAnswer;
    @Column(name = "is_correct")
    private Boolean correct;
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;
    @Column(name = "answered_at")
    private Instant answeredAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AssessmentItem() { }

    public static AssessmentItem unanswered(UUID id, UUID assessmentId, int sequenceNo,
            UUID cefrLevelId, AssessmentQuestion question, Instant createdAt) {
        Objects.requireNonNull(question, "question");
        if (sequenceNo < 1 || sequenceNo > 50) {
            throw new IllegalArgumentException("sequenceNo phải trong khoảng 1..50");
        }
        AssessmentItem item = new AssessmentItem();
        item.id = Objects.requireNonNull(id, "id");
        item.assessmentId = Objects.requireNonNull(assessmentId, "assessmentId");
        item.sequenceNo = sequenceNo;
        item.vocabularyId = question.vocabularyId();
        item.cefrLevelId = Objects.requireNonNull(cefrLevelId, "cefrLevelId");
        item.questionText = question.questionText();
        item.options = question.options();
        item.correctAnswer = question.correctAnswer();
        item.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        return item;
    }

    public UUID getId() { return id; }
    public UUID getAssessmentId() { return assessmentId; }
    public int getSequenceNo() { return sequenceNo; }
    public UUID getVocabularyId() { return vocabularyId; }
    public UUID getCefrLevelId() { return cefrLevelId; }
    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return List.copyOf(options); }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getSelectedAnswer() { return selectedAnswer; }
    public Boolean getCorrect() { return correct; }
    public Integer getResponseTimeMs() { return responseTimeMs; }
    public Instant getAnsweredAt() { return answeredAt; }
    public Instant getCreatedAt() { return createdAt; }
}
