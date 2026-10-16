package com.example.englishaicoach.gamification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Import(StreakIntegrationTests.Configuration.class)
class StreakIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired StreakService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @Autowired MutableClock clock;
    @MockitoSpyBean StreakRepository streaks;
    private final List<UUID> owned = new ArrayList<>();

    @BeforeEach void time() { clock.now = Instant.parse("2026-10-16T12:00:00Z"); }

    @AfterEach void cleanup() {
        for (UUID user : owned) {
            jdbc.update("DELETE FROM xp_logs WHERE user_id=?", user);
            jdbc.update("DELETE FROM streaks WHERE user_id=?", user);
            jdbc.update("DELETE FROM user_profiles WHERE user_id=?", user);
            jdbc.update("DELETE FROM users WHERE id=?", user);
        }
    }

    @ParameterizedTest
    @EnumSource(value = StreakActivity.class, names = {"SESSION_ATTEMPT", "QUIZ_ATTEMPT_ANSWER"})
    void acceptedActivityIncludingIncorrectNeedsNoCorrectnessInput(StreakActivity activity) {
        UUID user = user("UTC");
        assertThat((Boolean) tx().execute(status -> service.recordAcceptedActivity(user, activity))).isTrue();
        assertState(user, 1, 1, "2026-10-16", 1);
        assertThat(xp(user)).isEqualTo(10);
        assertThat(jdbc.queryForObject("SELECT updated_at FROM streaks WHERE user_id=?", Timestamp.class, user)
                .toInstant()).isEqualTo(clock.instant());
        UUID streak = jdbc.queryForObject("SELECT id FROM streaks WHERE user_id=?", UUID.class, user);
        assertThat(jdbc.queryForObject("SELECT source_event_id FROM xp_logs WHERE user_id=?", UUID.class, user))
                .isEqualTo(XpSource.streak(user, streak, LocalDate.parse("2026-10-16")).sourceEventId());
    }

    @Test void sameDayDifferentSourcesAndRetryPreserveEntireStateAndAward() {
        UUID user = user("UTC");
        activate(user);
        var before = jdbc.queryForMap("SELECT * FROM streaks WHERE user_id=?", user);
        var award = jdbc.queryForMap("SELECT * FROM xp_logs WHERE user_id=?", user);
        clock.now = clock.now.plusSeconds(100);
        assertThat((Boolean) tx().execute(status -> service.recordAcceptedActivity(user, StreakActivity.QUIZ_ATTEMPT_ANSWER)))
                .isFalse();
        assertThat(jdbc.queryForMap("SELECT * FROM streaks WHERE user_id=?", user)).isEqualTo(before);
        assertThat(jdbc.queryForMap("SELECT * FROM xp_logs WHERE user_id=?", user)).isEqualTo(award);
    }

    @Test void consecutiveDaysIncrementAndGapResetsWhileLongestIsPreserved() {
        UUID user = user("UTC");
        activate(user);
        clock.now = clock.now.plusSeconds(86400); activate(user);
        clock.now = clock.now.plusSeconds(86400); activate(user);
        assertState(user, 3, 3, "2026-10-18", 3);
        clock.now = clock.now.plusSeconds(2 * 86400); activate(user);
        assertState(user, 1, 3, "2026-10-20", 4);
        assertThat(xp(user)).isEqualTo(40);
    }

    @Test void existingEmptyStateStartsAtOneAndRetainsLongest() {
        UUID user = user("UTC");
        tx().executeWithoutResult(status -> streaks.initializeIfAbsent(user, clock.instant()));
        jdbc.update("UPDATE streaks SET longest_streak=7 WHERE user_id=?", user);
        activate(user);
        assertState(user, 1, 7, "2026-10-16", 1);
    }

    @Test void twoTimezonesUseDifferentDatesAtSameServerInstant() {
        clock.now = Instant.parse("2026-10-16T00:00:00Z");
        UUID east = user("Asia/Ho_Chi_Minh");
        UUID west = user("America/Los_Angeles");
        activate(east); activate(west);
        assertState(east, 1, 1, "2026-10-16", 1);
        assertState(west, 1, 1, "2026-10-15", 1);
    }

    @Test void localMidnightOneNanosecondChangesTheQualifyingDay() {
        UUID user = user("Asia/Ho_Chi_Minh");
        clock.now = Instant.parse("2026-10-15T16:59:59.999999999Z"); activate(user);
        assertState(user, 1, 1, "2026-10-15", 1);
        clock.now = clock.now.plusNanos(1); activate(user);
        assertState(user, 2, 2, "2026-10-16", 2);
        assertThat(xp(user)).isEqualTo(20);
    }

    @Test void dstRepeatedHourDoesNotAwardTwiceAndSpringGapStillUsesLocalDate() {
        UUID user = user("America/New_York");
        clock.now = Instant.parse("2026-03-08T06:59:59Z"); activate(user);
        clock.now = Instant.parse("2026-03-08T07:00:00Z");
        assertThat(activate(user)).isFalse();
        clock.now = Instant.parse("2026-11-01T05:30:00Z"); activate(user);
        clock.now = Instant.parse("2026-11-01T06:30:00Z");
        assertThat(activate(user)).isFalse();
        assertState(user, 1, 1, "2026-11-01", 2);
        assertThat(xp(user)).isEqualTo(20);
    }

    @Test void backwardsClockAndTimezoneChangeKeepHistoryUntilDateSurpassesLast() {
        UUID user = user("Pacific/Kiritimati");
        activate(user);
        assertState(user, 1, 1, "2026-10-17", 1);
        jdbc.update("UPDATE user_profiles SET timezone='Pacific/Honolulu' WHERE user_id=?", user);
        assertThat(activate(user)).isFalse();
        clock.now = clock.now.minusSeconds(86400);
        assertThat(activate(user)).isFalse();
        clock.now = Instant.parse("2026-10-18T12:00:00Z"); activate(user);
        assertState(user, 2, 2, "2026-10-18", 2);
        assertThat(xp(user)).isEqualTo(20);
    }

    @Test void assessmentDoesNotCreateStateOrRequireProfileAndPreservesExistingState() {
        UUID user = user(null);
        assertThat((Boolean) tx().execute(status -> service.recordAcceptedActivity(user, StreakActivity.ASSESSMENT))).isFalse();
        assertThat(count(user)).isZero();
        jdbc.update("INSERT INTO user_profiles(id,user_id,timezone,daily_learning_minutes,created_at,updated_at) "
                + "VALUES (?,?,'UTC',15,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", UUID.randomUUID(), user);
        activate(user);
        var before = jdbc.queryForMap("SELECT * FROM streaks WHERE user_id=?", user);
        clock.now = clock.now.plusSeconds(86400);
        assertThat((Boolean) tx().execute(status -> service.recordAcceptedActivity(user, StreakActivity.ASSESSMENT))).isFalse();
        assertThat(jdbc.queryForMap("SELECT * FROM streaks WHERE user_id=?", user)).isEqualTo(before);
        assertThat(xp(user)).isEqualTo(10);
    }

    @Test void missingOrInvalidTimezoneRollsBackCallerMutationAndDoesNotFallback() {
        for (String timezone : new String[] {null, "invalid-zone", "+07:00", "UTC "}) {
            UUID user = user(timezone);
            assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
                jdbc.update("UPDATE users SET full_name='Không lưu' WHERE id=?", user);
                service.recordAcceptedActivity(user, StreakActivity.SESSION_ATTEMPT);
            })).isInstanceOf(IllegalStateException.class);
            assertThat(jdbc.queryForObject("SELECT full_name FROM users WHERE id=?", String.class, user))
                    .isEqualTo("Người học streak");
            assertThat(count(user)).isZero(); assertThat(xp(user)).isZero();
        }
    }

    @Test void mandatoryTransactionAndCallerFailurePreventPartialStateOrXp() {
        UUID user = user("UTC");
        assertThatThrownBy(() -> service.recordAcceptedActivity(user, StreakActivity.SESSION_ATTEMPT))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
            service.recordAcceptedActivity(user, StreakActivity.SESSION_ATTEMPT);
            throw new IllegalStateException("Mô phỏng lỗi sau award");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(count(user)).isZero(); assertThat(xp(user)).isZero();
        activate(user); assertThat(xp(user)).isEqualTo(10);
    }

    @Test void ledgerInvariantFailureRollsBackTransitionAndCallerMutation() {
        UUID user = user("UTC"); activate(user);
        clock.now = clock.now.plusSeconds(86400);
        UUID streak = jdbc.queryForObject("SELECT id FROM streaks WHERE user_id=?", UUID.class, user);
        UUID source = XpSource.streak(user, streak, LocalDate.parse("2026-10-17")).sourceEventId();
        jdbc.update("INSERT INTO xp_logs(id,user_id,source_event_id,xp_amount,reason,reference_type,reference_id,created_at) "
                + "VALUES (?,?,?,99,'STREAK_MAINTAINED','streaks',?,CURRENT_TIMESTAMP)", UUID.randomUUID(), user, source, streak);
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
            jdbc.update("UPDATE users SET full_name='Không lưu' WHERE id=?", user);
            service.recordAcceptedActivity(user, StreakActivity.SESSION_ATTEMPT);
        })).isInstanceOf(IllegalStateException.class);
        assertState(user, 1, 1, "2026-10-16", 1);
        assertThat(jdbc.queryForObject("SELECT full_name FROM users WHERE id=?", String.class, user))
                .isEqualTo("Người học streak");
    }

    @Test void concurrentFirstActivityInitializesOneRowAndAwardsOnce() throws Exception {
        UUID user = user("UTC");
        coordinateReads(user);
        List<Object> outcomes = concurrent(user);
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        assertState(user, 1, 1, "2026-10-16", 1);
        assertThat(xp(user)).isEqualTo(10); assertThat(count(user)).isEqualTo(1);
    }

    @Test void concurrentSameVersionHasOneWinnerAndRollbackOfLoserSourceMutation() throws Exception {
        UUID user = user("UTC"); activate(user);
        clock.now = clock.now.plusSeconds(86400);
        coordinateReads(user);
        List<Object> outcomes = concurrent(user);
        assertThat(outcomes.stream().filter(Boolean.TRUE::equals).count()).isEqualTo(1);
        Throwable conflict = (Throwable) outcomes.stream().filter(Throwable.class::isInstance).findFirst().orElseThrow();
        assertThat(conflict).isInstanceOfAny(jakarta.persistence.OptimisticLockException.class,
                OptimisticLockingFailureException.class);
        assertState(user, 2, 2, "2026-10-17", 2);
        assertThat(xp(user)).isEqualTo(20);
        assertThat(jdbc.queryForObject("SELECT daily_learning_minutes FROM user_profiles WHERE user_id=?", Integer.class, user))
                .isEqualTo(16);
        assertThat(activate(user)).isFalse();
    }

    private void coordinateReads(UUID user) {
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger reads = new AtomicInteger();
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            if (reads.incrementAndGet() <= 2) barrier.await(10, TimeUnit.SECONDS);
            return result;
        }).when(streaks).findByUserId(eq(user));
    }

    private List<Object> concurrent(UUID user) throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var operation = (java.util.concurrent.Callable<Object>) () -> {
                try {
                    return tx().execute(status -> {
                        boolean changed = service.recordAcceptedActivity(user, StreakActivity.SESSION_ATTEMPT);
                        if (changed) jdbc.update("UPDATE user_profiles SET daily_learning_minutes=daily_learning_minutes+1 WHERE user_id=?", user);
                        return changed;
                    });
                } catch (RuntimeException failure) { return failure; }
            };
            var first = executor.submit(operation); var second = executor.submit(operation);
            return List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
        }
    }

    private boolean activate(UUID user) {
        return Boolean.TRUE.equals(tx().execute(status -> service.recordAcceptedActivity(user, StreakActivity.SESSION_ATTEMPT)));
    }
    private TransactionTemplate tx() { return new TransactionTemplate(transactions); }
    private int count(UUID user) {
        return jdbc.queryForObject("SELECT count(*) FROM streaks WHERE user_id=?", Integer.class, user);
    }
    private long xp(UUID user) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(xp_amount),0) FROM xp_logs WHERE user_id=?", Long.class, user);
    }
    private void assertState(UUID user, int current, int longest, String date, long version) {
        var state = jdbc.queryForMap("SELECT * FROM streaks WHERE user_id=?", user);
        assertThat(state.get("current_streak")).isEqualTo(current);
        assertThat(state.get("longest_streak")).isEqualTo(longest);
        assertThat(state.get("last_active_date").toString()).isEqualTo(date);
        assertThat(state.get("version")).isEqualTo(version);
    }
    private UUID user(String timezone) {
        UUID user = UUID.randomUUID(); owned.add(user);
        jdbc.update("""
                INSERT INTO users(id,email,auth_provider,full_name,role,status,failed_login_attempts,created_at,updated_at)
                VALUES (?,?,'GOOGLE','Người học streak','USER','ACTIVE',0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """, user, user + "@example.test");
        if (timezone != null) jdbc.update("""
                INSERT INTO user_profiles(id,user_id,timezone,daily_learning_minutes,created_at,updated_at)
                VALUES (?,?,?,15,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), user, timezone);
        return user;
    }

    @TestConfiguration static class Configuration {
        @Bean @Primary MutableClock streakClock() { return new MutableClock(); }
    }
    static final class MutableClock extends Clock {
        volatile Instant now;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(now, zone); }
        @Override public Instant instant() { return now; }
    }
}
