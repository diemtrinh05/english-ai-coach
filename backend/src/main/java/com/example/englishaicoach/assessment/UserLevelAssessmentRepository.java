package com.example.englishaicoach.assessment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserLevelAssessmentRepository {
    private final EntityManager entities;
    private final JdbcTemplate jdbc;
    private final AssessmentItemRepository items;

    public UserLevelAssessmentRepository(EntityManager entities, JdbcTemplate jdbc,
            AssessmentItemRepository items) {
        this.entities = entities;
        this.jdbc = jdbc;
        this.items = items;
    }

    /** Empty biểu thị partial-index conflict; caller quyết định API/idempotency ở task sau. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<UserLevelAssessment> insertInProgress(UUID assessmentId, UUID userId,
            AssessmentType type, UUID questionId, AssessmentQuestion firstQuestion, Instant now) {
        Objects.requireNonNull(assessmentId, "assessmentId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(firstQuestion, "firstQuestion");
        if (firstQuestion.difficulty() != CefrDifficulty.A1) {
            throw new IllegalArgumentException("Assessment phải bắt đầu tại A1");
        }
        UUID a1 = jdbc.queryForObject("SELECT id FROM cefr_levels WHERE code='A1'", UUID.class);
        AssessmentItem first = AssessmentItem.unanswered(questionId, assessmentId, 1, a1, firstQuestion, now);
        // Target đúng partial index, không bắt unique violation rồi tiếp tục transaction đã lỗi.
        int inserted = jdbc.update("""
                INSERT INTO user_level_assessments(id,user_id,assessment_type,status,
                    current_cefr_level_id,questions_answered,correct_answers,block_questions,
                    block_correct,stable_block_count,started_at,created_at,updated_at)
                VALUES (?,?,?,'IN_PROGRESS',?,0,0,0,0,0,?,?,?)
                ON CONFLICT (user_id) WHERE status='IN_PROGRESS' DO NOTHING
                """, assessmentId, userId, type.name(), a1,
                Timestamp.from(now), Timestamp.from(now), Timestamp.from(now));
        if (inserted == 0) return Optional.empty();
        items.persist(first);
        return Optional.of(entities.find(UserLevelAssessment.class, assessmentId));
    }

    @Transactional(readOnly = true)
    public Optional<UserLevelAssessment> findOwned(UUID assessmentId, UUID userId) {
        return ownedQuery(assessmentId, userId, false);
    }

    /** Dùng cùng caller transaction cho mọi thay đổi aggregate/items ở các task API sau. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<UserLevelAssessment> lockOwned(UUID assessmentId, UUID userId) {
        return ownedQuery(assessmentId, userId, true);
    }

    @Transactional(readOnly = true)
    public Optional<UserLevelAssessment> findInProgressByUserId(UUID userId) {
        return entities.createQuery("""
                SELECT a FROM UserLevelAssessment a
                WHERE a.userId=:userId AND a.status=:status
                """, UserLevelAssessment.class).setParameter("userId", userId)
                .setParameter("status", AssessmentStatus.IN_PROGRESS).getResultStream().findFirst();
    }

    private Optional<UserLevelAssessment> ownedQuery(UUID assessmentId, UUID userId, boolean lock) {
        var query = entities.createQuery("""
                SELECT a FROM UserLevelAssessment a WHERE a.id=:id AND a.userId=:userId
                """, UserLevelAssessment.class).setParameter("id", assessmentId)
                .setParameter("userId", userId);
        if (lock) query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return query.getResultStream().findFirst();
    }
}
