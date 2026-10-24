package com.example.englishaicoach.assessment;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AssessmentItemRepository {
    private final EntityManager entities;

    public AssessmentItemRepository(EntityManager entities) { this.entities = entities; }

    /** Caller khóa aggregate và cùng transaction với mutation; flush không phải commit. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void persist(AssessmentItem item) {
        entities.persist(item);
        entities.flush();
    }

    @Transactional(readOnly = true)
    public List<AssessmentItem> findOwnedInOrder(UUID assessmentId, UUID userId) {
        return entities.createQuery("""
                SELECT i FROM AssessmentItem i, UserLevelAssessment a
                WHERE i.assessmentId=a.id AND a.id=:assessmentId AND a.userId=:userId
                ORDER BY i.sequenceNo
                """, AssessmentItem.class).setParameter("assessmentId", assessmentId)
                .setParameter("userId", userId).getResultList();
    }

    @Transactional(readOnly = true)
    public Optional<AssessmentItem> findFirstUnansweredOwned(UUID assessmentId, UUID userId) {
        return entities.createQuery("""
                SELECT i FROM AssessmentItem i, UserLevelAssessment a
                WHERE i.assessmentId=a.id AND a.id=:assessmentId AND a.userId=:userId
                    AND i.answeredAt IS NULL
                ORDER BY i.sequenceNo
                """, AssessmentItem.class).setParameter("assessmentId", assessmentId)
                .setParameter("userId", userId).setMaxResults(1).getResultStream().findFirst();
    }
}
