package com.example.englishaicoach.common.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

import com.example.englishaicoach.common.exception.IdempotencyKeyReuseException;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class IdempotencyServiceIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private static final String ROUTE = "/api/v1/learning/attempts";

    @Autowired
    private IdempotencyService service;

    @MockitoSpyBean
    private IdempotencyRepository repository;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanIdempotencyRecords() {
        reset(repository);
        jdbc = new JdbcTemplate(dataSource);
        jdbc.update("DELETE FROM idempotency_keys");
    }

    @Test
    void storesFirstResultAndReplaysWithoutRepeatingMutation() {
        UUID userId = createUser();
        UUID eventId = UUID.randomUUID();
        AtomicInteger mutations = new AtomicInteger();
        IdempotencyRequest request = request(eventId, 4);

        IdempotencyResult<TestResponse> first = service.execute(
                userId, eventId, request, TestResponse.class,
                () -> {
                    mutations.incrementAndGet();
                    return new IdempotencyResult<>(201, new TestResponse("accepted", 4));
                });
        IdempotencyResult<TestResponse> replay = service.execute(
                userId, eventId, request(eventId, 4), TestResponse.class,
                () -> {
                    mutations.incrementAndGet();
                    return new IdempotencyResult<>(500, new TestResponse("unexpected", 0));
                });

        assertEquals(first, replay);
        assertEquals(1, mutations.get());
        assertEquals(1, countByEventId(eventId));
    }

    @Test
    void rejectsReuseForDifferentBodyEndpointOrUser() {
        UUID userId = createUser();
        UUID anotherUserId = createUser();
        UUID eventId = UUID.randomUUID();
        service.execute(
                userId, eventId, request(eventId, 4), TestResponse.class,
                () -> new IdempotencyResult<>(200, new TestResponse("ok", 4)));

        assertThrows(IdempotencyKeyReuseException.class, () -> service.execute(
                userId, eventId, request(eventId, 2), TestResponse.class,
                () -> new IdempotencyResult<>(200, new TestResponse("wrong", 2))));
        assertThrows(IdempotencyKeyReuseException.class, () -> service.execute(
                userId,
                eventId,
                new IdempotencyRequest("POST", "/api/v1/quizzes", "/api/v1/quizzes", Map.of(),
                        Map.of("eventId", eventId, "answerQuality", 4)),
                TestResponse.class,
                () -> new IdempotencyResult<>(200, new TestResponse("wrong", 4))));
        assertThrows(IdempotencyKeyReuseException.class, () -> service.execute(
                anotherUserId, eventId, request(eventId, 4), TestResponse.class,
                () -> new IdempotencyResult<>(200, new TestResponse("wrong", 4))));
    }

    @RepeatedTest(10)
    void concurrentIdenticalClaimsExecuteExactlyOneMutationAndReplayTheResult() throws Exception {
        UUID userId = createUser();
        UUID eventId = UUID.randomUUID();
        AtomicInteger mutations = new AtomicInteger();
        AtomicInteger claimCalls = new AtomicInteger();
        AtomicReference<Boolean> winnerClaimResult = new AtomicReference<>();
        AtomicReference<Boolean> loserClaimResult = new AtomicReference<>();
        CountDownLatch winnerClaimed = new CountDownLatch(1);
        CountDownLatch releaseWinner = new CountDownLatch(1);
        CountDownLatch loserReachedClaim = new CountDownLatch(1);

        doAnswer(invocation -> {
            int call = claimCalls.incrementAndGet();
            if (call == 1) {
                boolean inserted = (boolean) invocation.callRealMethod();
                winnerClaimResult.set(inserted);
                winnerClaimed.countDown();
                assertTrue(releaseWinner.await(10, TimeUnit.SECONDS));
                return inserted;
            }
            if (call == 2) {
                loserReachedClaim.countDown();
                boolean inserted = (boolean) invocation.callRealMethod();
                loserClaimResult.set(inserted);
                return inserted;
            }
            return invocation.callRealMethod();
        }).when(repository).claim(any(UUID.class), any(UUID.class), anyString(), anyString());

        try (var executor = Executors.newFixedThreadPool(2)) {
            var invocation = (java.util.concurrent.Callable<IdempotencyResult<TestResponse>>) () -> {
                return service.execute(
                        userId, eventId, request(eventId, 4), TestResponse.class,
                        () -> {
                            int mutationNumber = mutations.incrementAndGet();
                            jdbc.update("UPDATE users SET full_name = ? WHERE id = ?", "Mutation " + mutationNumber, userId);
                            return new IdempotencyResult<>(200, new TestResponse("accepted", mutationNumber));
                        });
            };
            Future<IdempotencyResult<TestResponse>> winner = executor.submit(invocation);
            assertTrue(winnerClaimed.await(10, TimeUnit.SECONDS));

            Future<IdempotencyResult<TestResponse>> loser = executor.submit(invocation);
            assertTrue(loserReachedClaim.await(10, TimeUnit.SECONDS));
            awaitLoserBlockedOnPostgreSqlClaim();
            releaseWinner.countDown();

            IdempotencyResult<TestResponse> winnerResult = winner.get(20, TimeUnit.SECONDS);
            IdempotencyResult<TestResponse> loserResult = loser.get(20, TimeUnit.SECONDS);
            assertEquals(winnerResult, loserResult);
            assertEquals(200, loserResult.status());
            assertEquals(new TestResponse("accepted", 1), loserResult.body());
        }

        assertEquals(2, claimCalls.get());
        assertEquals(Boolean.TRUE, winnerClaimResult.get());
        assertEquals(Boolean.FALSE, loserClaimResult.get());
        assertEquals(1, mutations.get());
        assertEquals("Mutation 1", jdbc.queryForObject(
                "SELECT full_name FROM users WHERE id = ?", String.class, userId));
        assertEquals(1, countByEventId(eventId));
    }

    private void awaitLoserBlockedOnPostgreSqlClaim() {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            Integer blockedClaims = jdbc.queryForObject("""
                    SELECT count(*)
                    FROM pg_stat_activity
                    WHERE datname = current_database()
                      AND state = 'active'
                      AND wait_event_type = 'Lock'
                      AND query ILIKE '%INSERT INTO idempotency_keys%'
                    """, Integer.class);
            if (blockedClaims != null && blockedClaims > 0) {
                return;
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20));
        }
        fail("Loser không chờ PostgreSQL lock tại câu INSERT idempotency claim");
    }

    @Test
    void businessFailureRollsBackMutationAndClaimSoSameEventCanRetry() {
        UUID userId = createUser();
        UUID eventId = UUID.randomUUID();

        assertThrows(IntentionalBusinessFailure.class, () -> service.execute(
                userId, eventId, request(eventId, 4), TestResponse.class,
                () -> {
                    jdbc.update("UPDATE users SET full_name = 'Không được commit' WHERE id = ?", userId);
                    throw new IntentionalBusinessFailure();
                }));

        assertEquals(0, countByEventId(eventId));
        assertEquals("Người kiểm thử", jdbc.queryForObject(
                "SELECT full_name FROM users WHERE id = ?", String.class, userId));

        IdempotencyResult<TestResponse> retry = service.execute(
                userId, eventId, request(eventId, 4), TestResponse.class,
                () -> new IdempotencyResult<>(200, new TestResponse("retried", 4)));
        assertEquals("retried", retry.body().message());
        assertEquals(1, countByEventId(eventId));
    }

    @Test
    void deletesOnlyRecordsOlderThanConfiguredThirtyDayRetention() {
        UUID userId = createUser();
        Instant reference = Instant.parse("2026-09-15T02:00:00Z");
        insertCompletedRecord(userId, reference.minus(31, ChronoUnit.DAYS));
        insertCompletedRecord(userId, reference.minus(30, ChronoUnit.DAYS));
        insertCompletedRecord(userId, reference.minus(1, ChronoUnit.DAYS));

        assertEquals(1, service.deleteExpired(reference));
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM idempotency_keys", Integer.class));
    }

    private IdempotencyRequest request(UUID eventId, int answerQuality) {
        return new IdempotencyRequest(
                "POST",
                ROUTE,
                ROUTE,
                Map.of("language", List.of("vi")),
                Map.of("eventId", eventId, "answerQuality", answerQuality));
    }

    private UUID createUser() {
        UUID userId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO users (
                    id, email, auth_provider, full_name, role, status,
                    failed_login_attempts, created_at, updated_at
                ) VALUES (?, ?, 'LOCAL', 'Người kiểm thử', 'USER', 'ACTIVE', 0, now(), now())
                """, userId, userId + "@example.test");
        return userId;
    }

    private int countByEventId(UUID eventId) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM idempotency_keys WHERE event_id = ?", Integer.class, eventId);
    }

    private void insertCompletedRecord(UUID userId, Instant createdAt) {
        jdbc.update("""
                INSERT INTO idempotency_keys (
                    event_id, user_id, endpoint, request_hash,
                    response_snapshot, response_status, created_at
                ) VALUES (?, ?, ?, ?, '{}'::jsonb, 200, ?)
                """, UUID.randomUUID(), userId, ROUTE, "a".repeat(64), Timestamp.from(createdAt));
    }

    record TestResponse(String message, int value) {
    }

    private static final class IntentionalBusinessFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
