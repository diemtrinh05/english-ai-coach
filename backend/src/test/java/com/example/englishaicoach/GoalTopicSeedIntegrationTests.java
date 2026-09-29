package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class GoalTopicSeedIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private static final String MIGRATION = "db/migration/V7__seed_demo_goal_topics.sql";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    void flywayAppliesGoalTopicSeedAfterVocabularyTopicSeed() {
        var migration = Arrays.stream(flyway.info().all())
                .filter(info -> info.getVersion() != null)
                .filter(info -> "7".equals(info.getVersion().getVersion()))
                .filter(info -> "V7__seed_demo_goal_topics.sql".equals(info.getScript()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Thiếu migration goal_topics V7"));

        assertEquals(MigrationState.SUCCESS, migration.getState());
    }

    @Test
    void everySpecializedGoalHasWeightedTopicsWithActiveVocabulary() {
        List<GoalCoverage> coverage = jdbc().query("""
                SELECT g.name,
                       COUNT(DISTINCT gt.topic_id) AS topic_count,
                       COUNT(DISTINCT v.id) AS vocabulary_count
                FROM goals g
                LEFT JOIN goal_topics gt ON gt.goal_id = g.id
                LEFT JOIN topics t ON t.id = gt.topic_id AND t.is_active = TRUE
                LEFT JOIN vocabulary_topics vt ON vt.topic_id = t.id
                LEFT JOIN vocabulary v ON v.id = vt.vocabulary_id AND v.is_active = TRUE
                WHERE g.is_active = TRUE
                GROUP BY g.name
                ORDER BY g.name
                """, (rs, row) -> new GoalCoverage(rs.getString("name"),
                rs.getLong("topic_count"), rs.getLong("vocabulary_count")));

        assertEquals(7, coverage.size());
        assertEquals(8L, coverage.stream()
                .filter(row -> row.goalName().equals("GENERAL_ENGLISH"))
                .findFirst().orElseThrow().topicCount());
        for (GoalCoverage row : coverage) {
            if (!row.goalName().equals("GENERAL_ENGLISH")) {
                assertTrue(row.topicCount() > 0, row.goalName() + " thiếu topic");
                assertTrue(row.vocabularyCount() > 0, row.goalName() + " thiếu vocabulary active");
            }
        }

        Integer invalidWeights = jdbc().queryForObject("""
                SELECT COUNT(*) FROM goal_topics
                WHERE relevance_weight < 0 OR relevance_weight > 1
                """, Integer.class);
        assertEquals(0, invalidWeights);

        Integer priority = jdbc().queryForObject("""
                SELECT COUNT(*)
                FROM goal_topics specialized
                JOIN goals g ON g.id = specialized.goal_id
                JOIN goal_topics general ON general.topic_id = specialized.topic_id
                JOIN goals fallback ON fallback.id = general.goal_id
                WHERE g.name <> 'GENERAL_ENGLISH'
                  AND fallback.name = 'GENERAL_ENGLISH'
                  AND specialized.relevance_weight > general.relevance_weight
                """, Integer.class);
        assertTrue(priority >= 6, "Các goal chuyên biệt cần có relevance cao hơn GENERAL_ENGLISH");
    }

    @Test
    void seedReplayPreservesRowsAndTimestamps() {
        List<String> before = jdbc().queryForList("""
                SELECT id::text || ':' || goal_id::text || ':' || topic_id::text
                       || ':' || relevance_weight::text || ':' || created_at::text
                FROM goal_topics ORDER BY id
                """, String.class);
        assertEquals(23, before.size());

        var populator = new ResourceDatabasePopulator(new ClassPathResource(MIGRATION));
        DatabasePopulatorUtils.execute(populator, dataSource);
        DatabasePopulatorUtils.execute(populator, dataSource);

        List<String> after = jdbc().queryForList("""
                SELECT id::text || ':' || goal_id::text || ':' || topic_id::text
                       || ':' || relevance_weight::text || ':' || created_at::text
                FROM goal_topics ORDER BY id
                """, String.class);
        assertEquals(before, after);
    }

    private JdbcTemplate jdbc() {
        return new JdbcTemplate(dataSource);
    }

    private record GoalCoverage(String goalName, long topicCount, long vocabularyCount) {
    }
}
