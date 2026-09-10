package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.sql.Connection;
import java.util.Arrays;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

class PostgreSqlHarnessIntegrationTests extends PostgreSqlIntegrationTestSupport {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private PostgreSQLContainer postgreSqlContainer;

    @Test
    void startsRealPostgreSqlAndRunsFlywayAutomatically() throws Exception {
        assertTrue(postgreSqlContainer.isRunning());
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
        }
        MigrationInfo[] migrations = flyway.info().all();
        MigrationInfo canonicalV1 = Arrays.stream(migrations)
                .filter(migration -> migration.getVersion() != null)
                .filter(migration -> "1".equals(migration.getVersion().getVersion()))
                .filter(migration -> "V1__create_schema_baseline.sql".equals(migration.getScript()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Thiếu canonical Flyway migration V1__create_schema_baseline.sql"));

        assertEquals(MigrationState.SUCCESS, canonicalV1.getState());
        assertTrue(
                Arrays.stream(migrations)
                        .filter(migration -> migration.getState().isResolved()
                                || migration.getState().isApplied())
                        .allMatch(migration -> migration.getState() == MigrationState.SUCCESS),
                () -> "Mọi Flyway migration đã resolved/applied phải ở trạng thái SUCCESS: "
                        + Arrays.toString(migrations));

        Integer tableCount = jdbc().queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name <> 'flyway_schema_history'
                """, Integer.class);

        assertEquals(34, tableCount);
    }

    @Test
    void usesNativeJsonbAndTimestampWithTimeZoneBehavior() {
        String language = jdbc().queryForObject(
                "SELECT (?::jsonb)->>'language'",
                String.class,
                "{\"language\":\"vi-VN\",\"enabled\":true}");
        Boolean sameInstant = jdbc().queryForObject(
                "SELECT ?::timestamptz = ?::timestamptz",
                Boolean.class,
                "2026-09-10T08:00:00+07:00",
                "2026-09-10T01:00:00Z");

        assertEquals("vi-VN", language);
        assertEquals(Boolean.TRUE, sameInstant);
    }

    @Test
    @Transactional
    void enforcesCanonicalCheckConstraintsInPostgreSql() {
        UUID userId = UUID.randomUUID();
        jdbc().update("""
                INSERT INTO users (
                    id, email, auth_provider, full_name, role, status,
                    failed_login_attempts, created_at, updated_at
                ) VALUES (?, ?, 'LOCAL', 'Người kiểm thử', 'USER', 'ACTIVE', 0, now(), now())
                """, userId, userId + "@example.test");

        assertThrows(DataIntegrityViolationException.class, () -> jdbc().update("""
                INSERT INTO idempotency_keys (
                    event_id, user_id, endpoint, request_hash, response_status, created_at
                ) VALUES (?, ?, '/api/v1/test', ?, 99, now())
                """, UUID.randomUUID(), userId, "a".repeat(64)));
    }

    private JdbcTemplate jdbc() {
        return new JdbcTemplate(dataSource);
    }
}
