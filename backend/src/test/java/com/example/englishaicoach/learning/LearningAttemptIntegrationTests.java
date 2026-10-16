package com.example.englishaicoach.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.auth.JwtAccessTokenService;
import com.example.englishaicoach.auth.PasswordHashService;
import com.example.englishaicoach.auth.User;
import com.example.englishaicoach.auth.UserRepository;
import com.example.englishaicoach.auth.UserRole;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import jakarta.persistence.EntityManager;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(LearningAttemptIntegrationTests.TimeConfiguration.class)
class LearningAttemptIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-16T08:15:30Z");
    private static final String SECRET = randomSecret();
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService tokens;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactions;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean LearningAttemptRepository repository;
    private final java.util.List<UUID> ownedUsers = new ArrayList<>();

    @AfterEach
    void cleanupOwnedFixtures() {
        for (UUID userId : ownedUsers) {
            jdbc.update("DELETE FROM session_attempts WHERE session_id IN (SELECT id FROM learning_sessions WHERE user_id=?)", userId);
            for (String table : java.util.List.of("idempotency_keys", "xp_logs", "streaks", "user_vocabulary_progress", "learning_sessions", "user_profiles"))
                jdbc.update("DELETE FROM " + table + " WHERE user_id=?", userId);
            jdbc.update("DELETE FROM users WHERE id=?", userId);
        }
    }

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) { registry.add("app.jwt.secret", () -> SECRET); }

    @Test
    void allQualitiesPersistCorrectnessSrsCountersAndSameDayXp() throws Exception {
        User user = user(true);
        UUID session = session(user, "NEW_WORDS");
        UUID word = word();
        int[] intervals = {1, 1, 1, 1, 7, 11};
        int[] repetitions = {0, 0, 0, 1, 2, 3};
        double[] factors = {1.7, 1.3, 1.3, 1.3, 1.3, 1.4};
        for (int q = 0; q <= 5; q++) {
            submit(user, request(session, word, q, 2000 + q)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.isCorrect").value(q >= 3))
                    .andExpect(jsonPath("$.answerQuality").value(q))
                    .andExpect(jsonPath("$.progress.vocabularyId").value(word.toString()))
                    .andExpect(jsonPath("$.progress.easeFactor").value(factors[q]))
                    .andExpect(jsonPath("$.progress.intervalDays").value(intervals[q]))
                    .andExpect(jsonPath("$.progress.repetitions").value(repetitions[q]))
                    .andExpect(jsonPath("$.progress.status").value(q < 4 ? "LEARNING" : "REVIEWING"))
                    .andExpect(jsonPath("$.progress.nextReviewAt").value(NOW.plusSeconds(intervals[q] * 86400L).toString()))
                    .andExpect(jsonPath("$.srs.algorithmVersion").value("sm2-ext-v1"));
        }
        var state = jdbc.queryForMap("SELECT * FROM user_vocabulary_progress WHERE user_id=?", user.getId());
        assertThat(state.get("correct_count")).isEqualTo(3);
        assertThat(state.get("incorrect_count")).isEqualTo(3);
        assertThat(state.get("avg_response_time_ms")).isEqualTo(2003);
        assertThat(((Number) state.get("last_quality")).intValue()).isEqualTo(5);
        assertThat(((Timestamp) state.get("last_reviewed_at")).toInstant()).isEqualTo(NOW);
        assertThat(state.get("version")).isEqualTo(6L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM session_attempts WHERE session_id=?",
                Integer.class, session)).isEqualTo(6);
        assertThat(jdbc.queryForObject("SELECT SUM(xp_amount) FROM xp_logs WHERE user_id=?",
                Integer.class, user.getId())).isEqualTo(25);
        assertThat(jdbc.queryForMap("SELECT words_studied_count,accuracy_percent FROM learning_sessions WHERE id=?", session))
                .containsEntry("words_studied_count", 1).containsEntry("accuracy_percent", new java.math.BigDecimal("50.00"));
    }

    @Test
    void aggregatesUseDistinctWordsAllSessionsAndNullableHistory() throws Exception {
        User user = user(true);
        UUID word = word();
        UUID first = session(user, "QUIZ");
        UUID second = session(user, "MIXED");
        submit(user, request(first, word, 5, 1000)).andExpect(status().isOk());
        jdbc.update("INSERT INTO session_attempts(id,session_id,vocabulary_id,attempt_type,is_correct,response_time_ms,answer_quality,attempted_at) VALUES (?,?,?,'FLASHCARD',false,NULL,0,?)",
                UUID.randomUUID(), first, word, Timestamp.from(NOW.minusSeconds(1)));
        submit(user, request(second, word, 0, 1001)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT avg_response_time_ms FROM user_vocabulary_progress WHERE user_id=?",
                Integer.class, user.getId())).isEqualTo(1001);
        submit(user, request(second, anotherWord(word), 5, 3000)).andExpect(status().isOk());
        submit(user, request(second, word, 5, 3000)).andExpect(status().isOk());
        assertThat(jdbc.queryForMap("SELECT words_studied_count,accuracy_percent FROM learning_sessions WHERE id=?", second))
                .containsEntry("words_studied_count", 2).containsEntry("accuracy_percent", new java.math.BigDecimal("66.67"));
        assertThat(jdbc.queryForObject("SELECT ended_at IS NULL FROM learning_sessions WHERE id=?", Boolean.class, second)).isTrue();
    }

    @Test
    void replaySurvivesClosureAndDeactivationWithoutRepeatingEffects() throws Exception {
        User user = user(true);
        UUID session = session(user, "REVIEW");
        UUID word = word();
        var request = request(session, word, 4, 2500);
        var first = submit(user, request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        jdbc.update("UPDATE learning_sessions SET ended_at=? WHERE id=?", Timestamp.from(NOW), session);
        jdbc.update("UPDATE vocabulary SET is_active=false WHERE id=?", word);
        try {
            var replay = submit(user, request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(mapper.readTree(replay)).isEqualTo(mapper.readTree(first));
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM session_attempts WHERE session_id=?", Integer.class, session)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT SUM(xp_amount) FROM xp_logs WHERE user_id=?", Integer.class, user.getId())).isEqualTo(15);
        } finally { jdbc.update("UPDATE vocabulary SET is_active=true WHERE id=?", word); }
    }

    @Test
    void sameEventRejectsPayloadQueryEndpointAndUserReuse() throws Exception {
        User user = user(true);
        var request = request(session(user, "MIXED"), word(), 4, 2500);
        submit(user, request).andExpect(status().isOk());
        var changed = new LinkedHashMap<>(request);
        changed.put("answerQuality", 1);
        submit(user, changed).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        submit(user(true), request).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        mvc.perform(post("/api/v1/learning/attempts?source=other").header("Authorization", "Bearer " + token(user))
                .contentType("application/json").content(mapper.writeValueAsString(request)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        mvc.perform(post("/api/v1/learning/sessions").header("Authorization", "Bearer " + token(user))
                .contentType("application/json").content(mapper.writeValueAsString(Map.of("eventId", request.get("eventId"), "sessionType", "MIXED"))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
    }

    @Test
    void strictJsonValidationRejectsCoercionUnknownMissingAndBoundsBeforeReplay() throws Exception {
        User user = user(true);
        var base = request(session(user, "MIXED"), word(), 4, 2500);
        submit(user, base).andExpect(status().isOk());
        for (String key : base.keySet()) {
            var changed = new LinkedHashMap<>(base);
            changed.remove(key);
            submit(user, changed).andExpect(status().isBadRequest());
            changed.put(key, null);
            submit(user, changed).andExpect(status().isBadRequest());
        }
        for (var change : java.util.List.of(Map.entry("answerQuality", (Object) "4"), Map.entry("answerQuality", 4.5),
                Map.entry("answerQuality", 6), Map.entry("answerQuality", -1), Map.entry("responseTimeMs", -1),
                Map.entry("responseTimeMs", 2147483648L), Map.entry("attemptType", 1), Map.entry("attemptType", "UNKNOWN"),
                Map.entry("isCorrect", true), Map.entry("userId", user.getId()), Map.entry("version", 1))) {
            var changed = new LinkedHashMap<>(base);
            changed.put(change.getKey(), change.getValue());
            submit(user, changed).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM session_attempts WHERE session_id=?", Integer.class, base.get("sessionId"))).isEqualTo(1);
    }

    @Test
    void ownershipActiveWordClosedSessionAndAuthenticationFailWithoutClaims() throws Exception {
        User owner = user(true);
        UUID session = session(owner, "MIXED");
        UUID word = word();
        var other = user(true);
        submit(other, request(session, word, 5, 1)).andExpect(status().isNotFound());
        submit(owner, request(UUID.randomUUID(), word, 5, 1)).andExpect(status().isNotFound());
        submit(owner, request(session, UUID.randomUUID(), 5, 1)).andExpect(status().isNotFound());
        jdbc.update("UPDATE vocabulary SET is_active=false WHERE id=?", word);
        try { submit(owner, request(session, word, 5, 1)).andExpect(status().isNotFound()); }
        finally { jdbc.update("UPDATE vocabulary SET is_active=true WHERE id=?", word); }
        jdbc.update("UPDATE learning_sessions SET ended_at=? WHERE id=?", Timestamp.from(NOW), session);
        submit(owner, request(session, word, 5, 1)).andExpect(status().isBadRequest());
        var body = mapper.writeValueAsString(request(session, word, 5, 1));
        mvc.perform(post("/api/v1/learning/attempts").contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/learning/attempts").header("Authorization", "Bearer " + tokens.issue(owner.getId(), UserRole.ADMIN))
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/learning/attempts").header("Authorization", "Bearer " + tokens.issue(UUID.randomUUID(), UserRole.USER))
                .contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id IN (?,?)", Integer.class, owner.getId(), other.getId())).isZero();
    }

    @Test
    void missingTimezoneRollsBackAllTablesAndSameEventCanRetry() throws Exception {
        User user = user(false);
        UUID session = session(user, "MIXED");
        var request = request(session, word(), 5, 0);
        submit(user, request).andExpect(status().isInternalServerError());
        assertEmptyMutation(user, session);
        profile(user);
        submit(user, request).andExpect(status().isOk());
    }

    @Test
    void snapshotWriteFailureRollsBackProgressAttemptXpStreakAndAggregates() throws Exception {
        User user = user(true);
        UUID session = session(user, "MIXED");
        var request = request(session, word(), 5, 1000);
        jdbc.execute("CREATE FUNCTION reject_attempt_snapshot_test() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$");
        jdbc.execute("CREATE TRIGGER reject_attempt_snapshot_test BEFORE UPDATE ON idempotency_keys FOR EACH ROW EXECUTE FUNCTION reject_attempt_snapshot_test()");
        try { submit(user, request).andExpect(status().isInternalServerError()); assertEmptyMutation(user, session); }
        finally {
            jdbc.execute("DROP TRIGGER reject_attempt_snapshot_test ON idempotency_keys");
            jdbc.execute("DROP FUNCTION reject_attempt_snapshot_test()");
        }
        submit(user, request).andExpect(status().isOk());
    }

    @Test
    void concurrentDuplicateProducesOneAtomicMutationAndIdenticalSnapshots() throws Exception {
        User user = user(true);
        UUID session = session(user, "MIXED");
        var body = request(session, word(), 5, 1000);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            ArrayList<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < 2; i++) calls.add(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return submit(user, body).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            });
            var results = pool.invokeAll(calls);
            assertThat(mapper.readTree(results.get(0).get(30, TimeUnit.SECONDS)))
                    .isEqualTo(mapper.readTree(results.get(1).get(30, TimeUnit.SECONDS)));
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM session_attempts WHERE session_id=?", Integer.class, session)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT SUM(xp_amount) FROM xp_logs WHERE user_id=?", Integer.class, user.getId())).isEqualTo(15);
    }

    @Test
    void staleProgressVersionRejectsSecondTransactionWithoutSilentOverwrite() throws Exception {
        User user = user(true);
        UUID word = word();
        new TransactionTemplate(transactions).executeWithoutResult(s -> repository.loadOrInitialize(user.getId(), word, NOW));
        CyclicBarrier barrier = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            ArrayList<Callable<Boolean>> calls = new ArrayList<>();
            for (int i = 0; i < 2; i++) calls.add(() -> {
                try {
                    new TransactionTemplate(transactions).executeWithoutResult(s -> {
                        var progress = repository.loadOrInitialize(user.getId(), word, NOW);
                        try { barrier.await(10, TimeUnit.SECONDS); } catch (Exception e) { throw new IllegalStateException(e); }
                        progress.updateAverageResponseTime(123);
                        repository.flush();
                    });
                    return true;
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException | jakarta.persistence.OptimisticLockException e) { return false; }
            });
            var results = pool.invokeAll(calls);
            assertThat(java.util.List.of(results.get(0).get(30, TimeUnit.SECONDS), results.get(1).get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        }
        assertThat(jdbc.queryForObject("SELECT version FROM user_vocabulary_progress WHERE user_id=?", Long.class, user.getId())).isEqualTo(1L);
    }

    @Test
    void concurrentHttpStaleVersionReturns409AndRollsBackLosingAttemptAndClaim() throws Exception {
        User user = user(true);
        UUID word = word();
        submit(user, request(session(user, "MIXED"), word, 5, 1000)).andExpect(status().isOk());
        var one = request(session(user, "MIXED"), word, 5, 1000);
        var two = request(session(user, "MIXED"), word, 5, 1000);
        CyclicBarrier loaded = new CyclicBarrier(2);
        org.mockito.Mockito.doAnswer(invocation -> {
            Object progress = invocation.callRealMethod();
            loaded.await(10, TimeUnit.SECONDS);
            return progress;
        }).when(repository).loadOrInitialize(user.getId(), word, NOW);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var results = pool.invokeAll(java.util.List.<Callable<org.springframework.mock.web.MockHttpServletResponse>>of(
                    () -> submit(user, one).andReturn().getResponse(), () -> submit(user, two).andReturn().getResponse()));
            var responses = java.util.List.of(results.get(0).get(30, TimeUnit.SECONDS), results.get(1).get(30, TimeUnit.SECONDS));
            assertThat(responses.stream().map(r -> r.getStatus()).toList()).containsExactlyInAnyOrder(200, 409);
            var conflict = responses.stream().filter(r -> r.getStatus() == 409).findFirst().orElseThrow();
            assertThat(mapper.readTree(conflict.getContentAsString()).get("code").asText()).isEqualTo("CONCURRENT_UPDATE");
        } finally { org.mockito.Mockito.doCallRealMethod().when(repository).loadOrInitialize(user.getId(), word, NOW); }
        assertThat(jdbc.queryForObject("SELECT correct_count FROM user_vocabulary_progress WHERE user_id=?", Integer.class, user.getId())).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE event_id IN (?,?)", Integer.class, one.get("eventId"), two.get("eventId"))).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM session_attempts a JOIN learning_sessions s ON s.id=a.session_id WHERE s.user_id=?", Integer.class, user.getId())).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT SUM(xp_amount) FROM xp_logs WHERE user_id=?", Integer.class, user.getId())).isEqualTo(20);
        assertThat(jdbc.queryForObject("SELECT SUM(words_studied_count) FROM learning_sessions WHERE id IN (?,?)", Integer.class, one.get("sessionId"), two.get("sessionId"))).isEqualTo(1);
    }

    @Test
    void concurrentFirstProgressInitializationNeverEscapesAs500OrDuplicatesState() throws Exception {
        User user = user(true);
        UUID word = word();
        var one = request(session(user, "MIXED"), word, 5, 1000);
        var two = request(session(user, "MIXED"), word, 5, 1000);
        CyclicBarrier start = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var results = pool.invokeAll(java.util.List.<Callable<Integer>>of(
                    () -> { start.await(10, TimeUnit.SECONDS); return submit(user, one).andReturn().getResponse().getStatus(); },
                    () -> { start.await(10, TimeUnit.SECONDS); return submit(user, two).andReturn().getResponse().getStatus(); }));
            var statuses = java.util.List.of(results.get(0).get(30, TimeUnit.SECONDS), results.get(1).get(30, TimeUnit.SECONDS));
            assertThat(statuses).allMatch(code -> code == 200 || code == 409).contains(200);
            int accepted = (int) statuses.stream().filter(code -> code == 200).count();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_vocabulary_progress WHERE user_id=?", Integer.class, user.getId())).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT correct_count FROM user_vocabulary_progress WHERE user_id=?", Integer.class, user.getId())).isEqualTo(accepted);
            assertThat(jdbc.queryForObject("SELECT SUM(xp_amount) FROM xp_logs WHERE user_id=?", Integer.class, user.getId())).isEqualTo(10 + 5 * accepted);
        }
    }

    @Test
    void databaseRejectsInconsistentCorrectness() {
        User user = user(true);
        UUID session = session(user, "MIXED");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO session_attempts(id,session_id,vocabulary_id,attempt_type,is_correct,answer_quality,attempted_at) VALUES (?,?,?,'FLASHCARD',true,0,?)",
                UUID.randomUUID(), session, word(), Timestamp.from(NOW))).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    private void assertEmptyMutation(User user, UUID session) {
        for (String table : java.util.List.of("user_vocabulary_progress", "xp_logs", "streaks", "idempotency_keys"))
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE user_id=?", Integer.class, user.getId())).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM session_attempts WHERE session_id=?", Integer.class, session)).isZero();
        assertThat(jdbc.queryForObject("SELECT words_studied_count FROM learning_sessions WHERE id=?", Integer.class, session)).isZero();
        assertThat(jdbc.queryForObject("SELECT accuracy_percent IS NULL FROM learning_sessions WHERE id=?", Boolean.class, session)).isTrue();
    }

    private org.springframework.test.web.servlet.ResultActions submit(User user, Map<String, ?> request) throws Exception {
        return mvc.perform(post("/api/v1/learning/attempts").header("Authorization", "Bearer " + token(user))
                .contentType("application/json").content(mapper.writeValueAsString(request)));
    }

    private Map<String, Object> request(UUID session, UUID word, int q, int time) {
        return Map.of("sessionId", session, "vocabularyId", word, "attemptType", "WORD_RECALL",
                "answerQuality", q, "responseTimeMs", time, "eventId", UUID.randomUUID());
    }

    private User user(boolean withProfile) {
        User user = users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Người học", passwords));
        ownedUsers.add(user.getId());
        if (withProfile) profile(user);
        return user;
    }
    private void profile(User user) {
        jdbc.update("INSERT INTO user_profiles(id,user_id,timezone,daily_learning_minutes,created_at,updated_at) VALUES (?,?,'Asia/Ho_Chi_Minh',15,?,?)",
                UUID.randomUUID(), user.getId(), Timestamp.from(NOW), Timestamp.from(NOW));
    }
    private UUID session(User user, String type) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO learning_sessions(id,user_id,session_type,started_at,words_studied_count) VALUES (?,?,?,?,0)", id, user.getId(), type, Timestamp.from(NOW));
        return id;
    }
    private UUID word() { return jdbc.queryForObject("SELECT id FROM vocabulary WHERE is_active=true ORDER BY id LIMIT 1", UUID.class); }
    private UUID anotherWord(UUID word) { return jdbc.queryForObject("SELECT id FROM vocabulary WHERE is_active=true AND id<>? ORDER BY id LIMIT 1", UUID.class, word); }
    private String token(User user) { return tokens.issue(user.getId(), UserRole.USER); }
    private static String randomSecret() {
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes); return Base64.getEncoder().encodeToString(bytes);
    }
    @TestConfiguration
    static class TimeConfiguration {
        @Bean @Primary Clock clock() { return Clock.fixed(NOW, ZoneOffset.UTC); }
    }
}
