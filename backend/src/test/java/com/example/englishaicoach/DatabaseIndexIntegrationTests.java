package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

class DatabaseIndexIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private static final Set<String> CRITICAL_INDEXES = Set.of(
            "uq_users_provider",
            "idx_user_vocabulary_progress_user_next_review",
            "uq_user_assessment_in_progress",
            "uq_notifications_user_type_local_date",
            "uq_user_goals_primary",
            "uq_user_vocabulary_progress_user_vocabulary",
            "streaks_user_id_key",
            "idx_idempotency_keys_user_created_at",
            "idx_idempotency_keys_endpoint_created_at",
            "idx_refresh_tokens_token_hash",
            "idx_refresh_tokens_user_expires_at",
            "idx_session_attempts_session_attempted_at",
            "idx_session_attempts_vocabulary_attempted_at",
            "uq_ai_reusable_generation_key");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    void appliesAppendOnlyCanonicalIndexMigration() {
        MigrationInfo canonicalV2 = Arrays.stream(flyway.info().all())
                .filter(migration -> migration.getVersion() != null)
                .filter(migration -> "2".equals(migration.getVersion().getVersion()))
                .filter(migration -> "V2__create_canonical_indexes.sql".equals(migration.getScript()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Thiếu migration V2__create_canonical_indexes.sql"));

        assertEquals(MigrationState.SUCCESS, canonicalV2.getState());
    }

    @Test
    void createsCriticalCanonicalIndexesWithPartialPredicates() {
        Map<String, String> indexDefinitions = jdbc().query(
                """
                SELECT indexname, indexdef
                FROM pg_indexes
                WHERE schemaname = 'public'
                """,
                resultSet -> {
                    var definitions = new java.util.HashMap<String, String>();
                    while (resultSet.next()) {
                        definitions.put(
                                resultSet.getString("indexname"),
                                resultSet.getString("indexdef"));
                    }
                    return definitions;
                });

        assertTrue(indexDefinitions.keySet().containsAll(CRITICAL_INDEXES),
                () -> "Thiếu index canonical: " + CRITICAL_INDEXES.stream()
                        .filter(index -> !indexDefinitions.containsKey(index))
                        .sorted()
                        .toList());
        assertIndexContains(indexDefinitions, "uq_users_provider", "provider_user_id is not null");
        assertIndexContains(indexDefinitions, "uq_user_goals_primary", "is_primary = true");
        assertIndexContains(indexDefinitions, "uq_user_assessment_in_progress", "where");
        assertIndexContains(indexDefinitions, "uq_user_assessment_in_progress", "in_progress");
        assertIndexContains(indexDefinitions, "uq_notifications_user_type_local_date", "where");
        assertIndexContains(indexDefinitions, "uq_notifications_user_type_local_date", "local_notification_date");
        assertIndexContains(indexDefinitions, "uq_notifications_user_type_local_date", "review_reminder");
        assertIndexContains(indexDefinitions, "uq_notifications_user_type_local_date", "daily_plan");
        assertIndexContains(indexDefinitions, "uq_notifications_user_type_local_date", "streak");
        assertIndexContains(indexDefinitions, "uq_ai_reusable_generation_key", "reusable");
    }

    @Test
    @Transactional
    void enforcesSinglePrimaryGoalPerUser() {
        UUID userId = insertUser();
        UUID firstGoalId = insertGoal("Mục tiêu thứ nhất");
        UUID secondGoalId = insertGoal("Mục tiêu thứ hai");
        UUID primaryGoalId = insertGoal("Mục tiêu chính");
        UUID duplicatePrimaryGoalId = insertGoal("Mục tiêu chính trùng");

        insertUserGoal(userId, firstGoalId, false);
        insertUserGoal(userId, secondGoalId, false);
        insertUserGoal(userId, primaryGoalId, true);

        assertThrows(DataIntegrityViolationException.class,
                () -> insertUserGoal(userId, duplicatePrimaryGoalId, true));
    }

    @Test
    @Transactional
    void enforcesSingleInProgressAssessmentPerUser() {
        UUID userId = insertUser();
        UUID cefrLevelId = insertCefrLevel();

        insertAssessment(userId, cefrLevelId, "COMPLETED");
        insertAssessment(userId, cefrLevelId, "CANCELLED");
        insertAssessment(userId, cefrLevelId, "IN_PROGRESS");

        assertThrows(DataIntegrityViolationException.class,
                () -> insertAssessment(userId, cefrLevelId, "IN_PROGRESS"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"REVIEW_REMINDER", "DAILY_PLAN", "STREAK"})
    @Transactional
    void rejectsDuplicateCanonicalReminderForSameUserTypeAndLocalDate(String notificationType) {
        UUID userId = insertUser();
        LocalDate localDate = LocalDate.of(2026, 9, 13);
        insertNotification(userId, notificationType, localDate);

        assertThrows(DataIntegrityViolationException.class,
                () -> insertNotification(userId, notificationType, localDate));
    }

    @Test
    @Transactional
    void allowsSystemDuplicatesAndNullLocalDatesOutsideNotificationPredicate() {
        UUID userId = insertUser();
        LocalDate localDate = LocalDate.of(2026, 9, 13);

        insertNotification(userId, "SYSTEM", localDate);
        insertNotification(userId, "SYSTEM", localDate);

        for (String notificationType : Set.of("REVIEW_REMINDER", "DAILY_PLAN", "STREAK")) {
            insertNotification(userId, notificationType, null);
            insertNotification(userId, notificationType, null);
        }
    }

    @Test
    void preservesOptimisticLockTargetDatabaseInvariants() {
        Map<String, Map<String, Object>> versionColumns = jdbc().query("""
                SELECT table_name, is_nullable, column_default
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND column_name = 'version'
                  AND table_name IN ('user_vocabulary_progress', 'streaks')
                """, resultSet -> {
                    var columns = new java.util.HashMap<String, Map<String, Object>>();
                    while (resultSet.next()) {
                        columns.put(resultSet.getString("table_name"), Map.of(
                                "nullable", resultSet.getString("is_nullable"),
                                "default", resultSet.getString("column_default")));
                    }
                    return columns;
                });

        assertEquals(Set.of("user_vocabulary_progress", "streaks"), versionColumns.keySet());
        versionColumns.values().forEach(column -> {
            assertEquals("NO", column.get("nullable"));
            assertEquals("0", column.get("default"));
        });
    }

    @Test
    @Transactional
    void dueReviewExplainUsesCanonicalCompositeIndex() {
        jdbc().execute("SET LOCAL enable_seqscan = off");
        String plan = jdbc().queryForList("""
                EXPLAIN (COSTS OFF)
                SELECT id
                FROM user_vocabulary_progress
                WHERE user_id = ?
                  AND next_review_at <= ?
                ORDER BY next_review_at ASC
                """, String.class, UUID.randomUUID(), OffsetDateTime.now())
                .stream()
                .collect(Collectors.joining("\n"));

        assertTrue(plan.contains("idx_user_vocabulary_progress_user_next_review"),
                () -> "EXPLAIN không dùng index due-review canonical:\n" + plan);
    }

    private void assertIndexContains(
            Map<String, String> indexDefinitions,
            String indexName,
            String expectedFragment) {
        String definition = indexDefinitions.get(indexName);
        assertNotNull(definition, () -> "Thiếu index " + indexName);
        assertTrue(definition.toLowerCase().contains(expectedFragment.toLowerCase()),
                () -> "Index " + indexName + " không chứa " + expectedFragment + ": " + definition);
    }

    private UUID insertUser() {
        UUID userId = UUID.randomUUID();
        jdbc().update("""
                INSERT INTO users (
                    id, email, auth_provider, full_name, role, status,
                    failed_login_attempts, created_at, updated_at
                ) VALUES (?, ?, 'LOCAL', 'Người kiểm thử', 'USER', 'ACTIVE', 0, now(), now())
                """, userId, userId + "@example.test");
        return userId;
    }

    private UUID insertGoal(String name) {
        UUID goalId = UUID.randomUUID();
        jdbc().update("""
                INSERT INTO goals (id, name, is_active, created_at)
                VALUES (?, ?, true, now())
                """, goalId, name + " " + goalId);
        return goalId;
    }

    private void insertUserGoal(UUID userId, UUID goalId, boolean primary) {
        jdbc().update("""
                INSERT INTO user_goals (id, user_id, goal_id, is_primary, created_at)
                VALUES (?, ?, ?, ?, now())
                """, UUID.randomUUID(), userId, goalId, primary);
    }

    private UUID insertCefrLevel() {
        return jdbc().queryForObject(
                "SELECT id FROM cefr_levels WHERE code = 'A1'",
                UUID.class);
    }

    private void insertAssessment(UUID userId, UUID cefrLevelId, String status) {
        jdbc().update("""
                INSERT INTO user_level_assessments (
                    id, user_id, assessment_type, status, current_cefr_level_id,
                    questions_answered, correct_answers, block_questions,
                    block_correct, stable_block_count, started_at, created_at, updated_at
                ) VALUES (?, ?, 'INITIAL', ?, ?, 0, 0, 0, 0, 0, now(), now(), now())
                """, UUID.randomUUID(), userId, status, cefrLevelId);
    }

    private void insertNotification(UUID userId, String notificationType, LocalDate localDate) {
        jdbc().update("""
                INSERT INTO notifications (
                    id, user_id, type, title, message, status,
                    local_notification_date, created_at
                ) VALUES (?, ?, ?, 'Thông báo', 'Nội dung', 'PENDING', CAST(? AS date), now())
                """, UUID.randomUUID(), userId, notificationType, localDate);
    }

    private JdbcTemplate jdbc() {
        return new JdbcTemplate(dataSource);
    }
}
