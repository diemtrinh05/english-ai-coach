package com.example.englishaicoach.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.auth.JwtAccessTokenService;
import com.example.englishaicoach.auth.PasswordHashService;
import com.example.englishaicoach.auth.User;
import com.example.englishaicoach.auth.UserRepository;
import com.example.englishaicoach.auth.UserRole;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(LearningSessionCompletionIntegrationTests.TimeConfiguration.class)
class LearningSessionCompletionIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-17T08:15:30Z");
    private static final String SECRET = randomSecret();
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService tokens;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @MockitoSpyBean LearningSessionRepository sessions;
    @MockitoSpyBean LearningAttemptRepository attempts;
    private final java.util.List<UUID> ownedUsers = new ArrayList<>();

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) { registry.add("app.jwt.secret", () -> SECRET); }

    @AfterEach
    void cleanup() {
        org.mockito.Mockito.reset(sessions, attempts);
        for (UUID userId : ownedUsers) {
            jdbc.update("DELETE FROM session_attempts WHERE session_id IN (SELECT id FROM learning_sessions WHERE user_id=?)", userId);
            for (String table : java.util.List.of("idempotency_keys", "xp_logs", "streaks", "user_vocabulary_progress", "learning_sessions", "user_profiles"))
                jdbc.update("DELETE FROM " + table + " WHERE user_id=?", userId);
            jdbc.update("DELETE FROM users WHERE id=?", userId);
        }
    }

    @Test
    void completionRecomputesDistinctWordsAndRoundedAccuracyForEverySessionType() throws Exception {
        User user = user();
        for (LearningSessionType type : LearningSessionType.values()) {
            UUID session = session(user, type);
            UUID word = word();
            history(session, word, true);
            history(session, word, false);
            history(session, anotherWord(word), true);
            complete(user, session, UUID.randomUUID()).andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value(session.toString()))
                    .andExpect(jsonPath("$.sessionType").value(type.name()))
                    .andExpect(jsonPath("$.startedAt").value(NOW.minusSeconds(60).toString()))
                    .andExpect(jsonPath("$.endedAt").value(NOW.toString()))
                    .andExpect(jsonPath("$.wordsStudiedCount").value(2))
                    .andExpect(jsonPath("$.accuracyPercent").value(66.67))
                    .andExpect(jsonPath("$.xp").doesNotExist());
            assertAward(user, session);
        }
        assertThat(jdbc.queryForObject("SELECT SUM(xp_amount) FROM xp_logs WHERE user_id=?", Integer.class, user.getId())).isEqualTo(40);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM streaks WHERE user_id=?", Integer.class, user.getId())).isZero();
    }

    @Test
    void incorrectAttemptQualifiesWithoutProfileAndClosedNewEventPreservesSummary() throws Exception {
        User user = user(); UUID session = session(user, LearningSessionType.QUIZ);
        history(session, word(), false);
        UUID event = UUID.randomUUID();
        String first = complete(user, session, event).andExpect(status().isOk())
                .andExpect(jsonPath("$.accuracyPercent").value(0.0)).andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(complete(user, session, UUID.randomUUID()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString())).isEqualTo(mapper.readTree(first));
        assertAward(user, session);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id=?", Integer.class, user.getId())).isEqualTo(2);
    }

    @Test
    void replayUsesSnapshotAndRejectsEndpointQueryAndUserReuse() throws Exception {
        User owner = user(); UUID session = session(owner, LearningSessionType.MIXED);
        history(session, word(), true); UUID event = UUID.randomUUID();
        String first = complete(owner, session, event).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        // Thay state để chứng minh replay đọc snapshot, còn event mới đọc summary hiện tại.
        jdbc.update("UPDATE learning_sessions SET ended_at=?,words_studied_count=7,accuracy_percent=12.34 WHERE id=?", Timestamp.from(NOW.plusSeconds(50)), session);
        assertThat(mapper.readTree(complete(owner, session, event).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()))
                .isEqualTo(mapper.readTree(first));
        complete(owner, session, UUID.randomUUID()).andExpect(status().isOk()).andExpect(jsonPath("$.wordsStudiedCount").value(7))
                .andExpect(jsonPath("$.endedAt").value(NOW.plusSeconds(50).toString()));
        complete(user(), session, event).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        complete(owner, UUID.randomUUID(), event).andExpect(status().isConflict());
        mvc.perform(post(route(session) + "?source=other").header("Authorization", "Bearer " + token(owner))
                .contentType("application/json").content(mapper.writeValueAsString(Map.of("eventId", event))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        mvc.perform(post("/api/v1/learning/sessions").header("Authorization", "Bearer " + token(owner))
                .contentType("application/json").content(mapper.writeValueAsString(Map.of("eventId", event, "sessionType", "MIXED"))))
                .andExpect(status().isConflict());
        assertAward(owner, session);
    }

    @Test
    void emptyMissingForeignAndInvalidCallerRollbackClaims() throws Exception {
        User owner = user(); UUID session = session(owner, LearningSessionType.MIXED); UUID event = UUID.randomUUID();
        complete(owner, session, event).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        complete(user(), session, UUID.randomUUID()).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        complete(owner, UUID.randomUUID(), UUID.randomUUID()).andExpect(status().isNotFound());
        mvc.perform(post(route(session)).contentType("application/json").content(mapper.writeValueAsString(Map.of("eventId", event))))
                .andExpect(status().isUnauthorized());
        for (var caller : java.util.List.of(Map.entry(tokens.issue(owner.getId(), UserRole.ADMIN), 403), Map.entry(tokens.issue(UUID.randomUUID(), UserRole.USER), 401))) {
            String access = caller.getKey();
            int expected = caller.getValue();
            mvc.perform(post(route(session)).header("Authorization", "Bearer " + access).contentType("application/json")
                    .content(mapper.writeValueAsString(Map.of("eventId", event)))).andExpect(status().is(expected));
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id=?", Integer.class, owner.getId())).isZero();
        assertThat(jdbc.queryForObject("SELECT ended_at IS NULL FROM learning_sessions WHERE id=?", Boolean.class, session)).isTrue();
        history(session, word(), false);
        complete(owner, session, event).andExpect(status().isOk());
    }

    @Test
    void strictJsonRejectsWrongTypesUnknownFieldsAndMalformedUuidBeforeReplay() throws Exception {
        User user = user(); UUID session = session(user, LearningSessionType.MIXED); history(session, word(), false);
        UUID event = UUID.randomUUID(); complete(user, session, event).andExpect(status().isOk());
        for (String body : java.util.List.of("{}", "null", "[]", "{\"eventId\":null}", "{\"eventId\":1}",
                "{\"eventId\":true}", "{\"eventId\":{}}", "{\"eventId\":\"invalid\"}", "{\"eventId\":\"1-1-1-1-1\"}",
                "{\"eventId\":\"" + event + "\",\"userId\":\"" + user.getId() + "\"}")) {
            mvc.perform(post(route(session)).header("Authorization", "Bearer " + token(user)).contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id=?", Integer.class, user.getId())).isEqualTo(1);
        assertAward(user, session);
    }

    @Test
    void xpFailureRollsBackClosureAggregatesAndClaimAndSameEventCanRetry() throws Exception {
        rollbackFailure("xp_logs", "INSERT");
    }

    @Test
    void snapshotFailureRollsBackClosureAggregatesXpAndClaimAndSameEventCanRetry() throws Exception {
        rollbackFailure("idempotency_keys", "UPDATE");
    }

    private void rollbackFailure(String table, String operation) throws Exception {
        User user = user(); UUID session = session(user, LearningSessionType.MIXED); history(session, word(), true);
        UUID event = UUID.randomUUID();
        jdbc.execute("CREATE FUNCTION reject_completion_test() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$");
        jdbc.execute("CREATE TRIGGER reject_completion_test BEFORE " + operation + " ON " + table + " FOR EACH ROW EXECUTE FUNCTION reject_completion_test()");
        try {
            complete(user, session, event).andExpect(status().isInternalServerError());
            assertThat(jdbc.queryForObject("SELECT ended_at IS NULL AND words_studied_count=0 AND accuracy_percent IS NULL FROM learning_sessions WHERE id=?", Boolean.class, session)).isTrue();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM xp_logs WHERE user_id=?", Integer.class, user.getId())).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE event_id=?", Integer.class, event)).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM session_attempts WHERE session_id=?", Integer.class, session)).isEqualTo(1);
        } finally {
            jdbc.execute("DROP TRIGGER reject_completion_test ON " + table);
            jdbc.execute("DROP FUNCTION reject_completion_test()");
        }
        complete(user, session, event).andExpect(status().isOk()); assertAward(user, session);
    }

    @Test
    void concurrentSameEventReplaysOneCompletion() throws Exception { concurrentCompletion(true); }

    @Test
    void concurrentDifferentEventsReturnSameSummaryAndAwardOnlyOnce() throws Exception { concurrentCompletion(false); }

    private void concurrentCompletion(boolean sameEvent) throws Exception {
        User user = user(); UUID session = session(user, LearningSessionType.REVIEW); history(session, word(), false);
        UUID event = UUID.randomUUID(); CyclicBarrier barrier = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var results = pool.invokeAll(java.util.List.<Callable<String>>of(
                    () -> { barrier.await(10, TimeUnit.SECONDS); return complete(user, session, event).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(); },
                    () -> { barrier.await(10, TimeUnit.SECONDS); return complete(user, session, sameEvent ? event : UUID.randomUUID()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(); }));
            assertThat(mapper.readTree(results.get(0).get(30, TimeUnit.SECONDS))).isEqualTo(mapper.readTree(results.get(1).get(30, TimeUnit.SECONDS)));
        }
        assertAward(user, session);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id=?", Integer.class, user.getId())).isEqualTo(sameEvent ? 1 : 2);
    }

    @Test
    void attemptHoldingSessionLockCommitsBeforeCompletionAndIsIncluded() throws Exception {
        orderedAttemptAndCompletion(true);
    }

    @Test
    void completionHoldingSessionLockClosesBeforeAttemptWhichIsRejected() throws Exception {
        orderedAttemptAndCompletion(false);
    }

    private void orderedAttemptAndCompletion(boolean attemptFirst) throws Exception {
        User user = user(); UUID session = session(user, LearningSessionType.MIXED); history(session, word(), false);
        jdbc.update("INSERT INTO user_profiles(id,user_id,timezone,daily_learning_minutes,created_at,updated_at) VALUES (?,?,'UTC',15,?,?)",
                UUID.randomUUID(), user.getId(), Timestamp.from(NOW), Timestamp.from(NOW));
        CountDownLatch locked = new CountDownLatch(1); CountDownLatch release = new CountDownLatch(1);
        org.mockito.stubbing.Answer<Object> pause = invocation -> {
            Object value = invocation.callRealMethod(); locked.countDown();
            if (!release.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("Hết thời gian chờ test lock.");
            return value;
        };
        if (attemptFirst) org.mockito.Mockito.doAnswer(pause).when(attempts).lockOwnedSession(session, user.getId());
        else org.mockito.Mockito.doAnswer(pause).when(sessions).lockOwned(session, user.getId());
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> attemptFirst ? submitAttempt(user, session) : complete(user, session, UUID.randomUUID()));
            assertThat(locked.await(15, TimeUnit.SECONDS)).isTrue();
            var second = pool.submit(() -> attemptFirst ? complete(user, session, UUID.randomUUID()) : submitAttempt(user, session));
            release.countDown();
            first.get(30, TimeUnit.SECONDS).andExpect(status().isOk());
            second.get(30, TimeUnit.SECONDS).andExpect(attemptFirst ? status().isOk() : status().isBadRequest());
        } finally { release.countDown(); }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM session_attempts WHERE session_id=?", Integer.class, session)).isEqualTo(attemptFirst ? 2 : 1);
        assertThat(jdbc.queryForObject("SELECT accuracy_percent FROM learning_sessions WHERE id=?", java.math.BigDecimal.class, session))
                .isEqualByComparingTo(attemptFirst ? "50.00" : "0.00");
        assertAward(user, session);
    }

    private ResultActions submitAttempt(User user, UUID session) throws Exception {
        return mvc.perform(post("/api/v1/learning/attempts").header("Authorization", "Bearer " + token(user)).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("eventId", UUID.randomUUID(), "sessionId", session, "vocabularyId", word(),
                        "attemptType", "FLASHCARD", "answerQuality", 5, "responseTimeMs", 1000))));
    }
    private ResultActions complete(User user, UUID session, UUID event) throws Exception {
        return mvc.perform(post(route(session)).header("Authorization", "Bearer " + token(user)).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("eventId", event))));
    }
    private static String route(UUID session) { return "/api/v1/learning/sessions/" + session + "/complete"; }
    private User user() {
        User user = users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Người học", passwords));
        ownedUsers.add(user.getId()); return user;
    }
    private UUID session(User user, LearningSessionType type) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO learning_sessions(id,user_id,session_type,started_at,words_studied_count) VALUES (?,?,?,?,0)", id, user.getId(), type.name(), Timestamp.from(NOW.minusSeconds(60)));
        return id;
    }
    private UUID word() { return jdbc.queryForObject("SELECT id FROM vocabulary WHERE is_active=true ORDER BY id LIMIT 1", UUID.class); }
    private UUID anotherWord(UUID word) { return jdbc.queryForObject("SELECT id FROM vocabulary WHERE is_active=true AND id<>? ORDER BY id LIMIT 1", UUID.class, word); }
    private void history(UUID session, UUID word, boolean correct) {
        jdbc.update("INSERT INTO session_attempts(id,session_id,vocabulary_id,attempt_type,is_correct,answer_quality,attempted_at) VALUES (?,?,?,'FLASHCARD',?,?,?)",
                UUID.randomUUID(), session, word, correct, correct ? 5 : 0, Timestamp.from(NOW));
    }
    private void assertAward(User user, UUID session) {
        var rows = jdbc.queryForList("SELECT xp_amount,source_event_id,reference_type,reference_id FROM xp_logs WHERE user_id=? AND reason='SESSION_COMPLETED' AND source_event_id=?", user.getId(), session);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).containsEntry("xp_amount", 10).containsEntry("reference_type", "learning_sessions").containsEntry("reference_id", session);
    }
    private String token(User user) { return tokens.issue(user.getId(), UserRole.USER); }
    private static String randomSecret() {
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes); return Base64.getEncoder().encodeToString(bytes);
    }
    @TestConfiguration
    static class TimeConfiguration { @Bean @Primary Clock clock() { return Clock.fixed(NOW, ZoneOffset.UTC); } }
}
