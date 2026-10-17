package com.example.englishaicoach.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.auth.JwtAccessTokenService;
import com.example.englishaicoach.auth.UserRole;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(LearningVerticalSliceIntegrationTests.TimeConfiguration.class)
class LearningVerticalSliceIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant START = Instant.parse("2026-10-17T16:59:30Z");
    private static final String SECRET = secret();
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired JwtAccessTokenService tokens;
    @Autowired MutableClock clock;
    private final List<UUID> ownedUsers = new ArrayList<>();

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> SECRET);
        registry.add("app.jwt.refresh-token-expiration", () -> "7d");
    }

    @BeforeEach
    void resetClock() { clock.now = START; }

    // Mỗi HTTP request commit thật; cleanup chỉ xóa fixture của chính test.
    @AfterEach
    void cleanup() {
        for (UUID user : ownedUsers) {
            jdbc.update("DELETE FROM session_attempts WHERE session_id IN (SELECT id FROM learning_sessions WHERE user_id=?)", user);
            for (String table : List.of("idempotency_keys", "xp_logs", "streaks", "user_vocabulary_progress",
                    "learning_sessions", "user_profiles", "refresh_tokens"))
                jdbc.update("DELETE FROM " + table + " WHERE user_id=?", user);
            jdbc.update("DELETE FROM users WHERE id=?", user);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE id=?", Integer.class, user)).isZero();
        }
    }

    @Test
    void committedApiJourneyConnectsProgressDueReviewCompletionAndLedgerSources() throws Exception {
        UUID user = register(true);
        List<UUID> words = words(2);
        JsonNode initial = progress(user, words.get(0));
        assertThat(initial.get("status").asString()).isEqualTo("NEW");
        assertThat(initial.get("nextReviewAt").isNull()).isTrue();
        assertThat(count("user_vocabulary_progress", user)).isZero();
        UUID session = start(user, "MIXED");
        JsonNode correct = accept(user, attempt(session, words.get(0), 5));
        JsonNode incorrect = accept(user, attempt(session, words.get(1), 2));
        assertThat(correct.get("isCorrect").asBoolean()).isTrue();
        assertThat(incorrect.get("isCorrect").asBoolean()).isFalse();
        assertThat(progress(user, words.get(0))).isEqualTo(correct.get("progress"));
        assertThat(progress(user, words.get(1))).isEqualTo(incorrect.get("progress"));
        assertThat(queue(user).get("count").asInt()).isZero();
        JsonNode summary = json(call(post(completeRoute(session)), user, event()), 200);
        assertThat(summary.get("wordsStudiedCount").asInt()).isEqualTo(2);
        assertThat(summary.get("accuracyPercent").asDouble()).isEqualTo(50.0);
        assertThat(summary.get("endedAt").asString()).isEqualTo(START.toString());
        assertThat(json(call(get(sessionRoute(session)), user, null), 200)).isEqualTo(summary);
        assertThat(json(call(post(completeRoute(session)), user, event()), 200)).isEqualTo(summary);
        assertThat(xp(user)).isEqualTo(25L);
        assertLearningAward(user, UUID.fromString(correct.get("attemptId").asString()));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=? AND source_event_id=? AND reason='SESSION_COMPLETED' AND reference_type='learning_sessions' AND reference_id=?", Integer.class, user, session, session)).isEqualTo(1);
        Map<String, Object> beforeReads = state(user);
        clock.now = START.plusSeconds(86400);
        JsonNode due = queue(user);
        assertThat(due.get("count").asInt()).isEqualTo(2);
        assertThat(due.get("items").get(0).get("vocabularyId").asString()).isEqualTo(words.get(0).toString());
        assertThat(due.get("items").get(1).get("vocabularyId").asString()).isEqualTo(words.get(1).toString());
        assertThat(due.toString()).doesNotContain("forgettingRisk");
        assertThat(state(user)).isEqualTo(beforeReads);
        UUID review = start(user, "REVIEW");
        JsonNode acceptedReview = accept(user, attempt(review, words.get(0), 5));
        assertThat(acceptedReview.get("progress").get("status").asString()).isEqualTo("REVIEWING");
        assertThat(acceptedReview.get("progress").get("intervalDays").asInt()).isEqualTo(6);
        assertThat(acceptedReview.get("progress").get("repetitions").asInt()).isEqualTo(2);
        assertThat(acceptedReview.get("progress").get("nextReviewAt").asString()).isEqualTo(clock.now.plusSeconds(6 * 86400L).toString());
        assertThat(queue(user).get("count").asInt()).isEqualTo(1);
        assertThat(queue(user).get("items").get(0).get("vocabularyId").asString()).isEqualTo(words.get(1).toString());
        assertThat(xp(user)).isEqualTo(40L);
    }

    @Test
    void eachQualityReachesDatabaseAndReadApisWithBackendCorrectness() throws Exception {
        UUID user = register(true);
        UUID session = start(user, "NEW_WORDS");
        List<UUID> words = words(6);
        String[] factors = {"1.70", "1.96", "2.18", "2.36", "2.50", "2.60"};
        for (int q = 0; q <= 5; q++) {
            UUID word = words.get(q);
            JsonNode result = accept(user, attempt(session, word, q));
            JsonNode current = progress(user, word);
            assertThat(result.get("isCorrect").asBoolean()).isEqualTo(q >= 3);
            assertThat(current).isEqualTo(result.get("progress"));
            assertThat(current.get("easeFactor").decimalValue()).isEqualByComparingTo(factors[q]);
            assertThat(current.get("intervalDays").asInt()).isEqualTo(1);
            assertThat(current.get("repetitions").asInt()).isEqualTo(q >= 3 ? 1 : 0);
            assertThat(current.get("correctCount").asInt()).isEqualTo(q >= 3 ? 1 : 0);
            assertThat(current.get("incorrectCount").asInt()).isEqualTo(q < 3 ? 1 : 0);
            assertThat(current.get("nextReviewAt").asString()).isEqualTo(START.plusSeconds(86400).toString());
            var row = jdbc.queryForMap("SELECT answer_quality,is_correct FROM session_attempts WHERE id=?", UUID.fromString(result.get("attemptId").asString()));
            assertThat(((Number) row.get("answer_quality")).intValue()).isEqualTo(q);
            assertThat(row.get("is_correct")).isEqualTo(q >= 3);
        }
        assertThat(xp(user)).isEqualTo(25L);
        assertThat(count("streaks", user)).isEqualTo(1);
        JsonNode summary = json(call(post(completeRoute(session)), user, event()), 200);
        assertThat(summary.get("accuracyPercent").asDouble()).isEqualTo(50.0);
        assertThat(summary.get("wordsStudiedCount").asInt()).isEqualTo(6);
        assertThat(xp(user)).isEqualTo(35L);
    }

    @Test
    void wrongAnswersAcrossLocalMidnightMaintainStreakWithoutCorrectAnswerXp() throws Exception {
        UUID user = register(true);
        UUID session = start(user, "REVIEW");
        UUID word = words(1).get(0);
        accept(user, attempt(session, word, 0));
        assertStreak(user, 1, "2026-10-17");
        clock.now = START.plusSeconds(60);
        accept(user, attempt(session, word, 1));
        assertStreak(user, 2, "2026-10-18");
        accept(user, attempt(session, word, 2));
        assertStreak(user, 2, "2026-10-18");
        assertThat(xp(user)).isEqualTo(20L);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=? AND reason='LEARNING_CORRECT'", Integer.class, user)).isZero();
        assertThat(queue(user).get("count").asInt()).isZero();
        for (String day : List.of("2026-10-17", "2026-10-18")) {
            UUID source = UUID.nameUUIDFromBytes(("gamification-v1:STREAK_MAINTAINED:" + user + ":" + day).getBytes(StandardCharsets.UTF_8));
            assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs x JOIN streaks s ON s.id=x.reference_id WHERE x.user_id=? AND x.source_event_id=? AND x.reason='STREAK_MAINTAINED' AND x.reference_type='streaks' AND x.xp_amount=10", Integer.class, user, source)).isEqualTo(1);
        }
        var streak = jdbc.queryForMap("SELECT * FROM streaks WHERE user_id=?", user);
        json(call(post(completeRoute(session)), user, event()), 200);
        assertThat(jdbc.queryForMap("SELECT * FROM streaks WHERE user_id=?", user)).isEqualTo(streak);
        assertThat(xp(user)).isEqualTo(30L);
    }

    @Test
    void rejectedRequestsPreserveBothUsersAndExplicitCorrectionCanContinue() throws Exception {
        UUID owner = register(true);
        UUID other = register(true);
        UUID session = start(owner, "MIXED");
        UUID word = words(1).get(0);
        var request = attempt(session, word, 5);
        Map<String, Object> before = state(owner);
        var forged = new LinkedHashMap<>(request);
        forged.put("isCorrect", true);
        error(call(post("/api/v1/learning/attempts"), owner, forged), 400, "VALIDATION_ERROR");
        error(call(post("/api/v1/learning/attempts"), other, request), 404, "NOT_FOUND");
        error(call(get(sessionRoute(session)), other, null), 404, "NOT_FOUND");
        error(call(post(completeRoute(session)), owner, event()), 400, "VALIDATION_ERROR");
        assertThat(state(owner)).isEqualTo(before);
        assertThat(count("xp_logs", other)).isZero();
        accept(owner, request);
        assertThat(progress(other, word).get("status").asString()).isEqualTo("NEW");
        assertThat(queue(other).get("count").asInt()).isZero();
        json(call(post(completeRoute(session)), owner, event()), 200);
        var closed = state(owner);
        error(call(post("/api/v1/learning/attempts"), owner, attempt(session, word, 5)), 400, "VALIDATION_ERROR");
        assertThat(state(owner)).isEqualTo(closed);
    }

    @Test
    void missingProfileFailureRollsBackAndSameOperationSucceedsAfterProfileSave() throws Exception {
        UUID user = register(false);
        UUID session = start(user, "MIXED");
        var request = attempt(session, words(1).get(0), 5);
        var before = state(user);
        error(call(post("/api/v1/learning/attempts"), user, request), 500, "INTERNAL_ERROR");
        assertThat(state(user)).isEqualTo(before);
        saveProfile(user);
        accept(user, request);
        assertThat(xp(user)).isEqualTo(15L);
    }

    @Test
    void lateAttemptSnapshotFailureRestoresPreviouslyCommittedStateAndRetrySucceeds() throws Exception {
        UUID user = register(true);
        UUID session = start(user, "MIXED");
        UUID word = words(1).get(0);
        accept(user, attempt(session, word, 5));
        clock.now = START.plusSeconds(86400);
        var request = attempt(session, word, 5);
        var before = state(user);
        rejectSnapshot(UUID.fromString(request.get("eventId").toString()));
        try {
            error(call(post("/api/v1/learning/attempts"), user, request), 500, "INTERNAL_ERROR");
            assertThat(state(user)).isEqualTo(before);
        } finally { removeSnapshotTrigger(); }
        accept(user, request);
        assertThat(xp(user)).isEqualTo(30L);
        assertStreak(user, 2, "2026-10-18");
        assertThat(progress(user, word).get("repetitions").asInt()).isEqualTo(2);
    }

    @Test
    void lateCompletionSnapshotFailurePreservesAcceptedAttemptAndRetryClosesOnce() throws Exception {
        UUID user = register(true);
        UUID session = start(user, "QUIZ");
        accept(user, attempt(session, words(1).get(0), 0));
        Map<String, Object> request = event();
        var before = state(user);
        rejectSnapshot(UUID.fromString(request.get("eventId").toString()));
        try {
            error(call(post(completeRoute(session)), user, request), 500, "INTERNAL_ERROR");
            assertThat(state(user)).isEqualTo(before);
        } finally { removeSnapshotTrigger(); }
        JsonNode summary = json(call(post(completeRoute(session)), user, request), 200);
        assertThat(summary.get("accuracyPercent").asDouble()).isZero();
        assertThat(summary.get("wordsStudiedCount").asInt()).isEqualTo(1);
        json(call(post(completeRoute(session)), user, request), 200);
        assertThat(xp(user)).isEqualTo(20L);
    }

    @Test
    void postgresRejectsOutOfRangeNullAndInconsistentQualityWithoutHttpValidation() throws Exception {
        UUID user = register(true);
        UUID session = start(user, "MIXED");
        UUID word = words(1).get(0);
        for (Object[] invalid : List.of(new Object[]{-1, false}, new Object[]{6, true},
                new Object[]{null, false}, new Object[]{2, true}, new Object[]{3, false})) {
            assertThatThrownBy(() -> jdbc.update("INSERT INTO session_attempts(id,session_id,vocabulary_id,attempt_type,answer_quality,is_correct,attempted_at) VALUES (?,?,?,'FLASHCARD',?,?,?)",
                    UUID.randomUUID(), session, word, invalid[0], invalid[1], Timestamp.from(START)))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .satisfies(error -> {
                        Throwable cause = error;
                        while (cause.getCause() != null) cause = cause.getCause();
                        assertThat(cause).isInstanceOf(java.sql.SQLException.class);
                        assertThat(((java.sql.SQLException) cause).getSQLState()).isIn("23514", "23502");
                    });
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM session_attempts WHERE session_id=?", Integer.class, session)).isZero();
        accept(user, attempt(session, word, 3));
        assertThat(xp(user)).isEqualTo(15L);
    }

    private UUID register(boolean profile) throws Exception {
        JsonNode result = json(call(post("/api/v1/auth/register"), null, Map.of("email", UUID.randomUUID() + "@example.test",
                "password", "password", "fullName", "Người học QA")), 201);
        UUID id = UUID.fromString(result.get("user").get("id").asString());
        ownedUsers.add(id);
        if (profile) saveProfile(id);
        return id;
    }
    private void saveProfile(UUID user) throws Exception {
        json(call(put("/api/v1/users/me/profile"), user, Map.of("fullName", "Người học QA", "timezone", "Asia/Ho_Chi_Minh", "dailyLearningMinutes", 15)), 200);
    }
    private UUID start(UUID user, String type) throws Exception {
        JsonNode response = json(call(post("/api/v1/learning/sessions"), user, Map.of("eventId", UUID.randomUUID(), "sessionType", type)), 201);
        return UUID.fromString(response.get("sessionId").asString());
    }
    private List<UUID> words(int limit) {
        return jdbc.queryForList("SELECT id FROM vocabulary WHERE is_active=true ORDER BY id LIMIT ?", UUID.class, limit);
    }
    private Map<String, Object> attempt(UUID session, UUID word, int quality) {
        return Map.of("eventId", UUID.randomUUID(), "sessionId", session, "vocabularyId", word,
                "attemptType", "FLASHCARD", "answerQuality", quality, "responseTimeMs", 3000);
    }
    private Map<String, Object> event() { return Map.of("eventId", UUID.randomUUID()); }
    private JsonNode accept(UUID user, Map<String, Object> request) throws Exception {
        return json(call(post("/api/v1/learning/attempts"), user, request), 200);
    }
    private JsonNode progress(UUID user, UUID word) throws Exception {
        return json(call(get("/api/v1/vocabulary/" + word + "/progress"), user, null), 200);
    }
    private JsonNode queue(UUID user) throws Exception { return json(call(get("/api/v1/learning/reviews"), user, null), 200); }
    private ResultActions call(MockHttpServletRequestBuilder request, UUID user, Object body) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + tokens.issue(user, UserRole.USER));
        if (body != null) request.contentType("application/json").content(mapper.writeValueAsString(body));
        return mvc.perform(request);
    }
    private JsonNode json(ResultActions response, int expected) throws Exception {
        return mapper.readTree(response.andExpect(status().is(expected)).andReturn().getResponse().getContentAsString());
    }
    private void error(ResultActions response, int expected, String code) throws Exception {
        response.andExpect(status().is(expected)).andExpect(jsonPath("$.code").value(code));
    }
    private static String sessionRoute(UUID session) { return "/api/v1/learning/sessions/" + session; }
    private static String completeRoute(UUID session) { return sessionRoute(session) + "/complete"; }
    private long count(String table, UUID user) { return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE user_id=?", Long.class, user); }
    private long xp(UUID user) { return jdbc.queryForObject("SELECT coalesce(sum(xp_amount),0) FROM xp_logs WHERE user_id=?", Long.class, user); }
    private void assertLearningAward(UUID user, UUID attempt) {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=? AND reason='LEARNING_CORRECT' AND source_event_id=? AND reference_id=? AND reference_type='session_attempts' AND xp_amount=5", Integer.class, user, attempt, attempt)).isEqualTo(1);
    }
    private void assertStreak(UUID user, int current, String day) {
        var row = jdbc.queryForMap("SELECT current_streak,longest_streak,last_active_date FROM streaks WHERE user_id=?", user);
        assertThat(row.get("current_streak")).isEqualTo(current);
        assertThat(row.get("longest_streak")).isEqualTo(current);
        assertThat(row.get("last_active_date").toString()).isEqualTo(day);
    }
    private Map<String, Object> state(UUID user) {
        var result = new LinkedHashMap<String, Object>();
        for (String table : List.of("learning_sessions", "user_vocabulary_progress", "xp_logs", "streaks"))
            result.put(table, jdbc.queryForList("SELECT * FROM " + table + " WHERE user_id=? ORDER BY id", user));
        result.put("session_attempts", jdbc.queryForList("SELECT a.* FROM session_attempts a JOIN learning_sessions s ON s.id=a.session_id WHERE s.user_id=? ORDER BY a.id", user));
        result.put("idempotency_keys", jdbc.queryForList("SELECT * FROM idempotency_keys WHERE user_id=? ORDER BY event_id", user));
        return result;
    }
    private void rejectSnapshot(UUID event) {
        jdbc.execute("CREATE FUNCTION qa_learn_snapshot_failure() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.event_id='" + event + "'::uuid THEN RAISE EXCEPTION 'QA injected snapshot failure'; END IF; RETURN NEW; END $$");
        jdbc.execute("CREATE TRIGGER qa_learn_snapshot_failure BEFORE UPDATE ON idempotency_keys FOR EACH ROW EXECUTE FUNCTION qa_learn_snapshot_failure()");
    }
    private void removeSnapshotTrigger() {
        jdbc.execute("DROP TRIGGER qa_learn_snapshot_failure ON idempotency_keys");
        jdbc.execute("DROP FUNCTION qa_learn_snapshot_failure()");
    }
    private static String secret() {
        byte[] value = new byte[32];
        new SecureRandom().nextBytes(value);
        return Base64.getEncoder().encodeToString(value);
    }
    @TestConfiguration
    static class TimeConfiguration {
        @Bean @Primary MutableClock time() { return new MutableClock(); }
    }
    static final class MutableClock extends Clock {
        private Instant now = START;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(now, zone); }
        @Override public Instant instant() { return now; }
    }
}
