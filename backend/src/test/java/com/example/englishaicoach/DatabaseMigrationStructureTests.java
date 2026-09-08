package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DatabaseMigrationStructureTests {

    private static final Pattern CREATE_TABLE = Pattern.compile(
            "(?im)^CREATE TABLE ([a-z][a-z0-9_]*)\\s*\\(");

    private static final Set<String> CANONICAL_TABLES = Set.of(
            "users",
            "refresh_tokens",
            "user_profiles",
            "goals",
            "user_goals",
            "goal_topics",
            "cefr_levels",
            "user_level_assessments",
            "assessment_items",
            "topics",
            "vocabulary",
            "vocabulary_topics",
            "vocabulary_examples",
            "user_vocabulary_progress",
            "learning_sessions",
            "session_attempts",
            "daily_plans",
            "daily_plan_items",
            "quizzes",
            "quiz_questions",
            "quiz_attempts",
            "quiz_attempt_answers",
            "streaks",
            "xp_logs",
            "badges",
            "user_badges",
            "notifications",
            "user_devices",
            "notification_preferences",
            "ai_generated_content",
            "ai_requests",
            "ai_usage_daily",
            "admin_audit_logs",
            "idempotency_keys");

    @Test
    void baselineMigrationDeclaresExactlyTheCanonicalTables() throws IOException {
        String resource = "db/migration/V1__create_schema_baseline.sql";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, () -> "Thiếu Flyway migration: " + resource);
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = CREATE_TABLE.matcher(sql);
            List<String> tables = new ArrayList<>();

            while (matcher.find()) {
                tables.add(matcher.group(1));
            }

            assertEquals(CANONICAL_TABLES.size(), tables.size(),
                    "Migration phải khai báo đúng 34 bảng, không trùng tên");
            assertEquals(CANONICAL_TABLES, Set.copyOf(tables),
                    "Danh sách bảng phải khớp Database Schema v1.6");
        }
    }
}
