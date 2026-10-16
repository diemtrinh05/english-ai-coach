package com.example.englishaicoach.gamification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.englishaicoach.auth.PasswordHashService;
import com.example.englishaicoach.auth.User;
import com.example.englishaicoach.auth.UserRepository;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class XpLedgerIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired GamificationService service;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired PlatformTransactionManager transactions;

    @ParameterizedTest
    @EnumSource(XpReason.class)
    void canonicalAwardAndRetryKeepOneImmutableRow(XpReason reason) {
        UUID user = user();
        XpSource source = source(user, reason);
        XpLog first = tx().execute(status -> service.award(source));
        XpLog retry = tx().execute(status -> service.award(source));
        int expected = switch (reason) {
            case LEARNING_CORRECT, QUIZ_CORRECT -> 5;
            case SESSION_COMPLETED, STREAK_MAINTAINED -> 10;
            case DAILY_PLAN_COMPLETED -> 50;
        };
        assertThat(first).isEqualTo(retry);
        assertThat(first.xpAmount()).isEqualTo(expected);
        assertThat(first.reason()).isEqualTo(reason.name());
        assertThat(first.referenceType()).isEqualTo(switch (reason) {
            case LEARNING_CORRECT -> "session_attempts";
            case QUIZ_CORRECT -> "quiz_attempt_answers";
            case SESSION_COMPLETED -> "learning_sessions";
            case DAILY_PLAN_COMPLETED -> "daily_plans";
            case STREAK_MAINTAINED -> "streaks";
        });
        assertThat(first.referenceId()).isEqualTo(source.referenceId());
        assertThat(first.sourceEventId()).isEqualTo(source.sourceEventId());
        assertThat(first.createdAt()).isNotNull();
        assertThat(count(user)).isEqualTo(1);
        assertThat(service.totalXp(user)).isEqualTo(expected);
    }

    @Test
    void totalIsUserScopedAndUsesWideSumWithEmptyZero() {
        UUID user = user();
        UUID other = user();
        assertThat(service.totalXp(user)).isZero();
        tx().executeWithoutResult(status -> {
            for (XpReason reason : XpReason.values()) service.award(source(user, reason));
            service.award(source(other, XpReason.DAILY_PLAN_COMPLETED));
        });
        assertThat(service.totalXp(user)).isEqualTo(80);
        assertThat(service.totalXp(other)).isEqualTo(50);
        // Ledger lịch sử có thể vượt INTEGER; SUM PostgreSQL trả BIGINT.
        for (int i = 0; i < 2; i++) jdbc.update("""
                INSERT INTO xp_logs(id,user_id,source_event_id,xp_amount,reason,created_at)
                VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), user, UUID.randomUUID(), Integer.MAX_VALUE, "LEGACY");
        assertThat(service.totalXp(user)).isEqualTo(80L + 2L * Integer.MAX_VALUE);
    }

    @Test
    void identitySeparatesUsersReasonsAndDistinctSourceEvents() {
        UUID user = user();
        UUID other = user();
        UUID event = UUID.randomUUID();
        tx().executeWithoutResult(status -> {
            service.award(XpSource.entity(user, XpReason.LEARNING_CORRECT, event));
            service.award(XpSource.entity(other, XpReason.LEARNING_CORRECT, event));
            service.award(XpSource.entity(user, XpReason.QUIZ_CORRECT, event));
            service.award(XpSource.entity(user, XpReason.LEARNING_CORRECT, UUID.randomUUID()));
        });
        assertThat(count(user)).isEqualTo(3);
        assertThat(count(other)).isEqualTo(1);
        assertThat(service.totalXp(user)).isEqualTo(15);
    }

    @Test
    void concurrentSameSourceCommitsOneAwardAndReturnsSameRow() throws Exception {
        UUID user = user();
        XpSource source = source(user, XpReason.LEARNING_CORRECT);
        var barrier = new CyclicBarrier(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var action = (java.util.concurrent.Callable<XpLog>) () -> {
                barrier.await(10, TimeUnit.SECONDS);
                return tx().execute(status -> service.award(source));
            };
            var first = executor.submit(action);
            var second = executor.submit(action);
            assertThat(first.get(30, TimeUnit.SECONDS)).isEqualTo(second.get(30, TimeUnit.SECONDS));
        }
        assertThat(count(user)).isEqualTo(1);
        assertThat(service.totalXp(user)).isEqualTo(5);
    }

    @Test
    void sourceMutationAndAwardRollbackTogetherAndRetryCanCommit() {
        UUID user = user();
        XpSource source = source(user, XpReason.SESSION_COMPLETED);
        String original = jdbc.queryForObject("SELECT full_name FROM users WHERE id=?", String.class, user);
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
            jdbc.update("UPDATE users SET full_name=? WHERE id=?", "Thay đổi trong transaction", user);
            service.award(source);
            throw new IllegalStateException("Mô phỏng lỗi nguồn");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(count(user)).isZero();
        assertThat(jdbc.queryForObject("SELECT full_name FROM users WHERE id=?", String.class, user))
                .isEqualTo(original);
        tx().executeWithoutResult(status -> service.award(source));
        assertThat(count(user)).isEqualTo(1);
    }

    @Test
    void conflictingMetadataRejectsAndRollsBackCallerMutation() {
        UUID user = user();
        XpSource source = source(user, XpReason.STREAK_MAINTAINED);
        XpLog first = tx().execute(status -> service.award(source));
        XpSource conflicting = new XpSource(user, source.reason(), source.sourceEventId(), UUID.randomUUID());
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
            jdbc.update("UPDATE users SET full_name='Không được lưu' WHERE id=?", user);
            service.award(conflicting);
        })).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("SELECT full_name FROM users WHERE id=?", String.class, user))
                .isEqualTo("Người học XP");
        XpLog retry = tx().execute(status -> service.award(source));
        assertThat(retry).isEqualTo(first);
        assertThat(count(user)).isEqualTo(1);
    }

    @Test
    void corruptedAmountOrReferenceTypeRejectsInsteadOfSilentReplay() {
        UUID user = user();
        XpSource source = source(user, XpReason.QUIZ_CORRECT);
        tx().executeWithoutResult(status -> service.award(source));
        jdbc.update("UPDATE xp_logs SET xp_amount=6 WHERE user_id=?", user);
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> service.award(source)))
                .isInstanceOf(IllegalStateException.class);
        jdbc.update("UPDATE xp_logs SET xp_amount=5,reference_type=NULL WHERE user_id=?", user);
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> service.award(source)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(count(user)).isEqualTo(1);
    }

    @Test
    void awardRequiresCallerTransaction() {
        UUID user = user();
        assertThatThrownBy(() -> service.award(source(user, XpReason.LEARNING_CORRECT)))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(count(user)).isZero();
    }

    @Test
    void streakIdentityMatchesApprovedUtf8NamespaceAndDayBoundary() {
        UUID user = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        UUID streak = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 10, 16);
        XpSource first = XpSource.streak(user, streak, date);
        assertThat(first.sourceEventId()).isEqualTo(UUID.nameUUIDFromBytes(
                "gamification-v1:STREAK_MAINTAINED:12345678-1234-1234-1234-123456789abc:2026-10-16"
                        .getBytes(StandardCharsets.UTF_8)));
        assertThat(first).isEqualTo(XpSource.streak(user, streak, date));
        assertThat(first.sourceEventId()).isNotEqualTo(XpSource.streak(user, streak, date.plusDays(1)).sourceEventId());
        assertThat(first.sourceEventId()).isNotEqualTo(XpSource.streak(UUID.randomUUID(), streak, date).sourceEventId());
        assertThatThrownBy(() -> XpSource.entity(user, XpReason.STREAK_MAINTAINED, streak))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new XpSource(user, XpReason.LEARNING_CORRECT, UUID.randomUUID(), streak))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void migrationBackfillsEachLegacyRowWithoutChangingHistoricalValues() throws Exception {
        String schema = "xp_migration_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
            try {
                connection.setSchema(schema);
                statement.execute("""
                        CREATE TABLE xp_logs(id UUID PRIMARY KEY,user_id UUID NOT NULL,xp_amount INTEGER NOT NULL,
                        reason VARCHAR(100) NOT NULL,reference_type VARCHAR(50),reference_id UUID,created_at TIMESTAMPTZ NOT NULL)
                        """);
                UUID id = UUID.randomUUID();
                UUID user = UUID.randomUUID();
                statement.execute("INSERT INTO xp_logs VALUES ('" + id + "','" + user
                        + "',17,'LEGACY',NULL,NULL,'2020-01-02T03:04:05Z')");
                ScriptUtils.executeSqlScript(connection,
                        new ClassPathResource("db/migration/V8__add_xp_source_event_identity.sql"));
                try (var rows = statement.executeQuery("SELECT * FROM xp_logs")) {
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getObject("id", UUID.class)).isEqualTo(id);
                    assertThat(rows.getObject("source_event_id", UUID.class)).isEqualTo(id);
                    assertThat(rows.getInt("xp_amount")).isEqualTo(17);
                    assertThat(rows.getString("reason")).isEqualTo("LEGACY");
                    assertThat(rows.getString("reference_type")).isNull();
                    assertThat(rows.getObject("reference_id")).isNull();
                    assertThat(rows.getTimestamp("created_at").toInstant().toString()).isEqualTo("2020-01-02T03:04:05Z");
                    assertThat(rows.next()).isFalse();
                }
                assertThatThrownBy(() -> statement.execute("INSERT INTO xp_logs(id,user_id,source_event_id,xp_amount,reason,created_at) VALUES ('"
                        + UUID.randomUUID() + "','" + user + "','" + id + "',17,'LEGACY',CURRENT_TIMESTAMP)"))
                        .isInstanceOf(java.sql.SQLException.class);
                assertThatThrownBy(() -> statement.execute("INSERT INTO xp_logs(id,user_id,xp_amount,reason,created_at) VALUES ('"
                        + UUID.randomUUID() + "','" + user + "',17,'LEGACY',CURRENT_TIMESTAMP)"))
                        .isInstanceOf(java.sql.SQLException.class);
            } finally {
                connection.setSchema("public");
                statement.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }

    private UUID user() {
        return users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password",
                "Người học XP", passwords)).getId();
    }

    private XpSource source(UUID user, XpReason reason) {
        return reason == XpReason.STREAK_MAINTAINED
                ? XpSource.streak(user, UUID.randomUUID(), LocalDate.of(2026, 10, 16))
                : XpSource.entity(user, reason, UUID.randomUUID());
    }

    private TransactionTemplate tx() {
        return new TransactionTemplate(transactions);
    }

    private int count(UUID user) {
        return jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=?", Integer.class, user);
    }
}
