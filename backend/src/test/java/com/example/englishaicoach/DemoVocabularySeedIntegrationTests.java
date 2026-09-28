package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
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

class DemoVocabularySeedIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private static final String MIGRATION = "db/migration/V4__seed_demo_vocabulary.sql";
    private static final List<String> CEFR_CODES = List.of("A1", "A2", "B1", "B2", "C1", "C2");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    void flywayAppliesDemoVocabularyMigration() {
        var migration = java.util.Arrays.stream(flyway.info().all())
                .filter(info -> info.getVersion() != null)
                .filter(info -> "4".equals(info.getVersion().getVersion()))
                .filter(info -> "V4__seed_demo_vocabulary.sql".equals(info.getScript()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Thiếu migration từ vựng demo V4"));

        assertEquals(MigrationState.SUCCESS, migration.getState());
    }

    @Test
    void seedsThirtyUsableActiveWordsForEachCefrLevel() {
        var rows = jdbc().queryForList("""
                SELECT c.code,
                       COUNT(v.id) FILTER (WHERE v.is_active = TRUE) AS active_count,
                       COUNT(v.id) FILTER (WHERE v.source = 'MANUAL'
                           AND v.is_active = TRUE
                           AND NULLIF(BTRIM(v.word), '') IS NOT NULL
                           AND NULLIF(BTRIM(v.part_of_speech), '') IS NOT NULL
                           AND NULLIF(BTRIM(v.meaning_vi), '') IS NOT NULL) AS usable_count,
                       COUNT(DISTINCT v.meaning_vi) FILTER (WHERE v.is_active = TRUE) AS distinct_meanings
                FROM cefr_levels c
                LEFT JOIN vocabulary v ON v.cefr_level_id = c.id
                GROUP BY c.code, c.sort_order
                ORDER BY c.sort_order
                """);

        assertEquals(CEFR_CODES, rows.stream().map(row -> (String) row.get("code")).toList());
        for (var row : rows) {
            assertEquals(30L, ((Number) row.get("active_count")).longValue(), row.get("code") + " active");
            assertEquals(30L, ((Number) row.get("usable_count")).longValue(), row.get("code") + " usable");
            assertEquals(30L, ((Number) row.get("distinct_meanings")).longValue(), row.get("code") + " meanings");
        }
    }

    @Test
    void naturalKeysAreUniqueAndSeedCanBeReappliedWithoutMutation() {
        Integer duplicates = jdbc().queryForObject("""
                SELECT COUNT(*) FROM (
                    SELECT word, part_of_speech, cefr_level_id
                    FROM vocabulary
                    GROUP BY word, part_of_speech, cefr_level_id
                    HAVING COUNT(*) > 1
                ) AS duplicate_keys
                """, Integer.class);
        assertEquals(0, duplicates);

        List<String> before = jdbc().queryForList("""
                SELECT id::text || ':' || created_at::text || ':' || updated_at::text
                FROM vocabulary ORDER BY id
                """, String.class);
        assertEquals(180, before.size());

        var populator = new ResourceDatabasePopulator(new ClassPathResource(MIGRATION));
        DatabasePopulatorUtils.execute(populator, dataSource);
        DatabasePopulatorUtils.execute(populator, dataSource);

        List<String> after = jdbc().queryForList("""
                SELECT id::text || ':' || created_at::text || ':' || updated_at::text
                FROM vocabulary ORDER BY id
                """, String.class);
        assertEquals(before, after);
        assertTrue(after.stream().allMatch(value -> !value.isBlank()));
    }

    private JdbcTemplate jdbc() {
        return new JdbcTemplate(dataSource);
    }
}
