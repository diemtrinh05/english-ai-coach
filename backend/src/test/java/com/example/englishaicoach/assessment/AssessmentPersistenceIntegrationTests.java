package com.example.englishaicoach.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class AssessmentPersistenceIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-24T02:03:04.123456Z");
    private static final List<String> OPTIONS = List.of("  nghĩa đúng  ", "nghĩa \\\"hai\\\"", "dòng\nhai", "漢字 — ba");
    @Autowired UserLevelAssessmentRepository assessments;
    @Autowired AssessmentItemRepository items;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    private final List<UUID> users = new ArrayList<>();
    private final List<UUID> words = new ArrayList<>();

    @AfterEach void cleanup() {
        for (UUID user : users) {
            jdbc.update("DELETE FROM assessment_items WHERE assessment_id IN (SELECT id FROM user_level_assessments WHERE user_id=?)", user);
            jdbc.update("DELETE FROM user_level_assessments WHERE user_id=?", user);
            jdbc.update("DELETE FROM users WHERE id=?", user);
        }
        for (UUID word : words) jdbc.update("DELETE FROM vocabulary WHERE id=?", word);
    }

    @ParameterizedTest
    @EnumSource(AssessmentType.class)
    void atomicInitialAggregateAndQuestionRoundTripAllTypes(AssessmentType type) {
        UUID user = user(), word = word(), id = UUID.randomUUID(), questionId = UUID.randomUUID();
        var saved = tx().execute(s -> assessments.insertInProgress(id, user, type, questionId, question(word), NOW).orElseThrow());
        assertThat(saved.getId()).isEqualTo(id);
        var a = assessments.findOwned(id, user).orElseThrow();
        assertThat(a.getUserId()).isEqualTo(user);
        assertThat(a.getAssessmentType()).isEqualTo(type);
        assertThat(a.getStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);
        assertThat(a.getCurrentCefrLevelId()).isEqualTo(level("A1"));
        assertThat(a.getFinalCefrLevelId()).isNull();
        assertThat(a.getScore()).isNull();
        assertThat(a.getQuestionsAnswered()).isZero();
        assertThat(a.getCorrectAnswers()).isZero();
        assertThat(a.getBlockQuestions()).isZero();
        assertThat(a.getBlockCorrect()).isZero();
        assertThat(a.getStableBlockCount()).isZero();
        assertThat(a.getStartedAt()).isEqualTo(NOW);
        assertThat(a.getCreatedAt()).isEqualTo(NOW);
        assertThat(a.getUpdatedAt()).isEqualTo(NOW);
        assertThat(a.getCompletedAt()).isNull();
        assertThat(assessments.findInProgressByUserId(user).orElseThrow().getId()).isEqualTo(id);
        var i = items.findFirstUnansweredOwned(id, user).orElseThrow();
        assertThat(i.getId()).isEqualTo(questionId);
        assertThat(i.getAssessmentId()).isEqualTo(id);
        assertThat(i.getSequenceNo()).isEqualTo(1);
        assertThat(i.getVocabularyId()).isEqualTo(word);
        assertThat(i.getCefrLevelId()).isEqualTo(level("A1"));
        assertThat(i.getQuestionText()).isEqualTo(question(word).questionText());
        assertThat(i.getOptions()).containsExactlyElementsOf(OPTIONS);
        assertThat(i.getCorrectAnswer()).isEqualTo(OPTIONS.getFirst());
        assertThat(i.getSelectedAnswer()).isNull();
        assertThat(i.getCorrect()).isNull();
        assertThat(i.getResponseTimeMs()).isNull();
        assertThat(i.getAnsweredAt()).isNull();
        assertThat(i.getCreatedAt()).isEqualTo(NOW);
        assertThatThrownBy(() -> i.getOptions().add("thay đổi")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(jdbc.queryForObject("SELECT jsonb_typeof(options_json) FROM assessment_items WHERE id=?", String.class, questionId))
                .isEqualTo("array");
    }

    @Test void questionOrderAndRawSnapshotSurviveCatalogChangeAndReadsDoNotWrite() {
        UUID user = user(), word1 = word(), word2 = word(), id = create(user, word1);
        UUID second = UUID.randomUUID();
        tx().executeWithoutResult(s -> items.persist(AssessmentItem.unanswered(second, id, 2, level("A1"), question(word2), NOW)));
        jdbc.update("UPDATE vocabulary SET word='changed',meaning_vi='khác',is_active=false WHERE id=?", word1);
        var before = jdbc.queryForList("SELECT * FROM assessment_items WHERE assessment_id=? ORDER BY sequence_no", id);
        assertThat(items.findOwnedInOrder(id, user)).extracting(AssessmentItem::getSequenceNo).containsExactly(1, 2);
        var first = items.findFirstUnansweredOwned(id, user).orElseThrow();
        assertThat(first.getQuestionText()).isEqualTo(question(word1).questionText());
        assertThat(first.getOptions()).containsExactlyElementsOf(OPTIONS);
        assertThat(first.getCorrectAnswer()).isEqualTo(OPTIONS.getFirst());
        assertThat(jdbc.queryForList("SELECT * FROM assessment_items WHERE assessment_id=? ORDER BY sequence_no", id)).isEqualTo(before);
        jdbc.update("UPDATE assessment_items SET selected_answer=?,is_correct=false,response_time_ms=0,answered_at=? WHERE id=?",
                OPTIONS.get(1), Timestamp.from(NOW.plusSeconds(1)), first.getId());
        assertThat(items.findFirstUnansweredOwned(id, user).orElseThrow().getId()).isEqualTo(second);
        var answered = items.findOwnedInOrder(id, user).getFirst();
        assertThat(answered.getSelectedAnswer()).isEqualTo(OPTIONS.get(1));
        assertThat(answered.getCorrect()).isFalse();
        assertThat(answered.getResponseTimeMs()).isZero();
        assertThat(answered.getAnsweredAt()).isEqualTo(NOW.plusSeconds(1));
        jdbc.update("UPDATE assessment_items SET answered_at=? WHERE id=?", Timestamp.from(NOW.plusSeconds(2)), second);
        assertThat(items.findFirstUnansweredOwned(id, user)).isEmpty();
    }

    @Test void allOwnedQueriesHideForeignAndMissingResources() {
        UUID user = user(), foreign = user(), id = create(user, word());
        assertThat(assessments.findOwned(id, foreign)).isEmpty();
        var foreignLock = tx().execute(s -> assessments.lockOwned(id, foreign));
        assertThat(foreignLock).isEmpty();
        assertThat(assessments.findOwned(UUID.randomUUID(), user)).isEmpty();
        assertThat(items.findOwnedInOrder(id, foreign)).isEmpty();
        assertThat(items.findFirstUnansweredOwned(id, foreign)).isEmpty();
        assertThat(items.findOwnedInOrder(UUID.randomUUID(), user)).isEmpty();
        assertThat(assessments.findInProgressByUserId(foreign)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = AssessmentStatus.class, names = {"COMPLETED", "CANCELLED"})
    void historicalRowsMapEveryStateFieldAndDoNotPreventNewInProgress(AssessmentStatus state) {
        UUID user = user(), id = create(user, word());
        jdbc.update("""
                UPDATE user_level_assessments SET status=?,current_cefr_level_id=?,final_cefr_level_id=?,
                    score=75.25,questions_answered=24,correct_answers=18,block_questions=0,block_correct=0,
                    stable_block_count=2,completed_at=?,updated_at=? WHERE id=?
                """, state.name(), level("B1"), level("B1"), Timestamp.from(NOW.plusSeconds(3)), Timestamp.from(NOW.plusSeconds(4)), id);
        var a = assessments.findOwned(id, user).orElseThrow();
        assertThat(a.getStatus()).isEqualTo(state);
        assertThat(a.getCurrentCefrLevelId()).isEqualTo(level("B1"));
        assertThat(a.getFinalCefrLevelId()).isEqualTo(level("B1"));
        assertThat(a.getScore()).isEqualByComparingTo(new BigDecimal("75.25"));
        assertThat(a.getQuestionsAnswered()).isEqualTo(24);
        assertThat(a.getCorrectAnswers()).isEqualTo(18);
        assertThat(a.getBlockQuestions()).isZero();
        assertThat(a.getBlockCorrect()).isZero();
        assertThat(a.getStableBlockCount()).isEqualTo(2);
        assertThat(a.getCompletedAt()).isEqualTo(NOW.plusSeconds(3));
        assertThat(a.getUpdatedAt()).isEqualTo(NOW.plusSeconds(4));
        assertThat(assessments.findInProgressByUserId(user)).isEmpty();
        assertThat(create(user, word())).isNotEqualTo(id);
        // Đây chỉ là mapping fixture SQL, không định nghĩa mutation/cancel/completion policy.
    }

    @Test void rollbackAfterFlushRemovesBothRowsAndQuestionFkFailureRollsBackAggregate() {
        UUID user = user(), word = word(), id = UUID.randomUUID();
        assertThatThrownBy(() -> tx().executeWithoutResult(s -> {
            assessments.insertInProgress(id, user, AssessmentType.INITIAL, UUID.randomUUID(), question(word), NOW).orElseThrow();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assessment_items WHERE assessment_id=?", Integer.class, id)).isEqualTo(1);
            throw new IllegalStateException("kiểm tra rollback caller");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(assessments.findOwned(id, user)).isEmpty();
        assertThat(countItems(id)).isZero();
        assertThatThrownBy(() -> tx().executeWithoutResult(s -> assessments.insertInProgress(
                id, user, AssessmentType.INITIAL, UUID.randomUUID(), question(UUID.randomUUID()), NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(assessments.findOwned(id, user)).isEmpty();
        assertThat(countItems(id)).isZero();
    }

    @Test void duplicateInProgressReturnsEmptyAndCallerTransactionRemainsUsable() {
        UUID user = user(), word = word(), winner = create(user, word), loser = UUID.randomUUID();
        tx().executeWithoutResult(s -> {
            assertThat(assessments.insertInProgress(loser, user, AssessmentType.MANUAL, UUID.randomUUID(), question(word), NOW)).isEmpty();
            assertThat(jdbc.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
            assertThat(assessments.findInProgressByUserId(user).orElseThrow().getId()).isEqualTo(winner);
        });
        assertThat(assessments.findOwned(loser, user)).isEmpty();
        assertThat(countItems(loser)).isZero();
    }

    @Test void concurrentCreatesCommitOneAggregateAndOneFirstQuestionWithoutAbortingLoserTransactions() throws Exception {
        UUID user = user(), word = word();
        CountDownLatch ready = new CountDownLatch(4), go = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(4)) {
            var calls = new ArrayList<java.util.concurrent.Future<Boolean>>();
            for (int i = 0; i < 4; i++) calls.add(pool.submit(() -> {
                ready.countDown();
                if (!go.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("timeout start");
                return tx().execute(s -> {
                    boolean inserted = assessments.insertInProgress(UUID.randomUUID(), user,
                            AssessmentType.INITIAL, UUID.randomUUID(), question(word), NOW).isPresent();
                    assertThat(jdbc.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
                    return inserted;
                });
            }));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            go.countDown();
            int accepted = 0;
            for (var call : calls) if (call.get(30, TimeUnit.SECONDS)) accepted++;
            assertThat(accepted).isEqualTo(1);
        }
        UUID id = assessments.findInProgressByUserId(user).orElseThrow().getId();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_level_assessments WHERE user_id=?", Integer.class, user)).isEqualTo(1);
        assertThat(countItems(id)).isEqualTo(1);
    }

    @Test void rowLockIsHeldUntilCallerCommit() throws Exception {
        UUID user = user(), id = create(user, word());
        CountDownLatch locked = new CountDownLatch(1), release = new CountDownLatch(1), secondStarted = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> tx().executeWithoutResult(s -> {
                assertThat(assessments.lockOwned(id, user)).isPresent();
                locked.countDown();
                try { if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("timeout release"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
            }));
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();
            var second = pool.submit(() -> tx().execute(s -> {
                secondStarted.countDown();
                return assessments.lockOwned(id, user).isPresent();
            }));
            try {
                assertThat(secondStarted.await(10, TimeUnit.SECONDS)).isTrue();
                assertThatThrownBy(() -> second.get(300, TimeUnit.MILLISECONDS)).isInstanceOf(java.util.concurrent.TimeoutException.class);
            } finally { release.countDown(); }
            first.get(10, TimeUnit.SECONDS);
            assertThat(second.get(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test void itemUniquenessAndResponseTimeCheckAreEnforcedByPostgres() {
        UUID user = user(), word = word(), secondWord = word(), id = create(user, word);
        assertThatThrownBy(() -> tx().executeWithoutResult(s -> items.persist(
                AssessmentItem.unanswered(UUID.randomUUID(), id, 1, level("A1"), question(secondWord), NOW))))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> tx().executeWithoutResult(s -> items.persist(
                AssessmentItem.unanswered(UUID.randomUUID(), id, 2, level("A1"), question(word), NOW))))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE assessment_items SET response_time_ms=-1 WHERE assessment_id=?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(countItems(id)).isEqualTo(1);
    }

    @Test void writeAndLockMethodsRequireCallerTransactionAndInvalidInitialDifficultyWritesNothing() {
        UUID user = user(), word = word();
        assertThatThrownBy(() -> assessments.insertInProgress(UUID.randomUUID(), user, AssessmentType.INITIAL,
                UUID.randomUUID(), question(word), NOW)).isInstanceOf(IllegalTransactionStateException.class);
        assertThatThrownBy(() -> assessments.lockOwned(UUID.randomUUID(), user)).isInstanceOf(IllegalTransactionStateException.class);
        assertThatThrownBy(() -> items.persist(AssessmentItem.unanswered(UUID.randomUUID(), UUID.randomUUID(),
                1, level("A1"), question(word), NOW))).isInstanceOf(IllegalTransactionStateException.class);
        var wrongLevel = new AssessmentQuestion(word, CefrDifficulty.A2, "câu hỏi", OPTIONS, OPTIONS.getFirst());
        assertThatThrownBy(() -> tx().executeWithoutResult(s -> assessments.insertInProgress(UUID.randomUUID(), user,
                AssessmentType.INITIAL, UUID.randomUUID(), wrongLevel, NOW)))
                .isInstanceOf(InvalidDataAccessApiUsageException.class).hasRootCauseInstanceOf(IllegalArgumentException.class);
        assertThat(assessments.findInProgressByUserId(user)).isEmpty();
    }

    private UUID create(UUID user, UUID word) {
        return tx().execute(s -> assessments.insertInProgress(UUID.randomUUID(), user, AssessmentType.INITIAL,
                UUID.randomUUID(), question(word), NOW).orElseThrow().getId());
    }
    private AssessmentQuestion question(UUID word) {
        return new AssessmentQuestion(word, CefrDifficulty.A1, "Từ 'raw \\\"word\\\"' có nghĩa là gì?", OPTIONS, OPTIONS.getFirst());
    }
    private UUID level(String code) { return jdbc.queryForObject("SELECT id FROM cefr_levels WHERE code=?", UUID.class, code); }
    private int countItems(UUID assessment) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM assessment_items WHERE assessment_id=?", Integer.class, assessment);
    }
    private TransactionTemplate tx() { var tx = new TransactionTemplate(transactions); tx.setTimeout(30); return tx; }
    private UUID user() {
        UUID id = UUID.randomUUID(); users.add(id);
        jdbc.update("""
                INSERT INTO users(id,email,auth_provider,full_name,role,status,failed_login_attempts,created_at,updated_at)
                VALUES (?,?,'GOOGLE','Kiểm thử assessment','USER','ACTIVE',0,?,?)
                """, id, id + "@assessment.invalid", Timestamp.from(NOW), Timestamp.from(NOW));
        return id;
    }
    private UUID word() {
        UUID id = UUID.randomUUID(); words.add(id);
        jdbc.update("""
                INSERT INTO vocabulary(id,word,meaning_vi,part_of_speech,cefr_level_id,source,is_active,created_at,updated_at)
                VALUES (?,?,?,'noun',?,'MANUAL',true,?,?)
                """, id, id.toString(), OPTIONS.getFirst(), level("A1"), Timestamp.from(NOW), Timestamp.from(NOW));
        return id;
    }
}
