package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class ReferenceDataIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private static final String SEED_MIGRATION =
            "db/migration/V3__seed_reference_data.sql";

    private static final List<CefrLevel> CANONICAL_CEFR_LEVELS = List.of(
            new CefrLevel("A1", "A1", 1),
            new CefrLevel("A2", "A2", 2),
            new CefrLevel("B1", "B1", 3),
            new CefrLevel("B2", "B2", 4),
            new CefrLevel("C1", "C1", 5),
            new CefrLevel("C2", "C2", 6));

    private static final List<String> CANONICAL_GOALS = List.of(
            "GENERAL_ENGLISH",
            "TRAVEL",
            "BUSINESS",
            "TOEIC",
            "IELTS",
            "COMMUNICATION",
            "ACADEMIC");

    private static final List<BadgeRule> CANONICAL_BADGES = List.of(
            new BadgeRule("First Lesson", "FIRST_LESSON", 1),
            new BadgeRule("7 Day Streak", "STREAK_7", 7),
            new BadgeRule("100 Words", "WORDS_100", 100),
            new BadgeRule("500 Words", "WORDS_500", 500),
            new BadgeRule("Perfect Quiz", "PERFECT_QUIZ", 5));

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    void appliesCanonicalReferenceDataMigration() {
        var canonicalV3 = Arrays.stream(flyway.info().all())
                .filter(migration -> migration.getVersion() != null)
                .filter(migration -> "3".equals(migration.getVersion().getVersion()))
                .filter(migration -> "V3__seed_reference_data.sql".equals(migration.getScript()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Thiếu migration V3__seed_reference_data.sql"));

        assertEquals(MigrationState.SUCCESS, canonicalV3.getState());
    }

    @Test
    void seedsCanonicalCefrLevelsInStableOrder() {
        List<CefrLevel> actual = jdbc().query("""
                SELECT code, name, sort_order
                FROM cefr_levels
                ORDER BY sort_order
                """, (resultSet, rowNumber) -> new CefrLevel(
                resultSet.getString("code"),
                resultSet.getString("name"),
                resultSet.getInt("sort_order")));

        assertEquals(CANONICAL_CEFR_LEVELS, actual);
    }

    @Test
    void seedsAllCanonicalGoalsAsActive() {
        List<String> actual = jdbc().queryForList("""
                SELECT name
                FROM goals
                WHERE is_active = TRUE
                ORDER BY id
                """, String.class);

        assertEquals(CANONICAL_GOALS, actual);
    }

    @Test
    void seedsFiveCanonicalBadgeRules() {
        List<BadgeRule> actual = jdbc().query("""
                SELECT name, condition_type, condition_value
                FROM badges
                WHERE is_active = TRUE
                ORDER BY id
                """, (resultSet, rowNumber) -> new BadgeRule(
                resultSet.getString("name"),
                resultSet.getString("condition_type"),
                resultSet.getInt("condition_value")));

        assertEquals(CANONICAL_BADGES, actual);
    }

    @Test
    void seedMigrationCanBeAppliedRepeatedlyWithoutDuplicates() {
        var populator = new ResourceDatabasePopulator(
                new ClassPathResource(SEED_MIGRATION));

        DatabasePopulatorUtils.execute(populator, dataSource);
        DatabasePopulatorUtils.execute(populator, dataSource);

        assertEquals(6, count("cefr_levels"));
        assertEquals(7, count("goals"));
        assertEquals(5, count("badges"));
    }

    private int count(String table) {
        return jdbc().queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    private JdbcTemplate jdbc() {
        return new JdbcTemplate(dataSource);
    }

    private record CefrLevel(String code, String name, int sortOrder) {
    }

    private record BadgeRule(String name, String conditionType, int conditionValue) {
    }
}
