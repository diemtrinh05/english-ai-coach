package com.example.englishaicoach.common.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.englishaicoach.auth.JwtAccessTokenService;
import com.example.englishaicoach.auth.PasswordHashService;
import com.example.englishaicoach.auth.User;
import com.example.englishaicoach.auth.UserRepository;
import com.example.englishaicoach.auth.UserRole;
import com.example.englishaicoach.gamification.StreakRepository;
import com.example.englishaicoach.learning.LearningAttemptRepository;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(LearningConcurrencyContractIntegrationTests.TimeConfiguration.class)
class LearningConcurrencyContractIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant START = Instant.parse("2026-10-18T08:00:00Z");
    private static final String SECRET = secret();
    private static final String ATTEMPT = "/api/v1/learning/attempts";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService tokens;
    @Autowired MutableClock clock;
    @MockitoSpyBean IdempotencyRepository claims;
    @MockitoSpyBean LearningAttemptRepository attempts;
    @MockitoSpyBean StreakRepository streaks;
    private final List<UUID> ownedUsers = new ArrayList<>();

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> SECRET);
        // Chặn treo test nếu regression làm SQL chờ khóa vô hạn.
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "SET statement_timeout = '15s'");
    }

    @BeforeEach
    void resetTime() { clock.now = START; }

    @AfterEach
    void cleanup() {
        reset(claims, attempts, streaks);
        // Chỉ xóa fixture sở hữu; từng HTTP request đã commit/rollback thật.
        for (UUID user : ownedUsers) {
            jdbc.update("DELETE FROM session_attempts WHERE session_id IN (SELECT id FROM learning_sessions WHERE user_id=?)", user);
            for (String table : List.of("idempotency_keys", "xp_logs", "streaks", "user_vocabulary_progress", "learning_sessions", "user_profiles"))
                jdbc.update("DELETE FROM " + table + " WHERE user_id=?", user);
            jdbc.update("DELETE FROM users WHERE id=?", user);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE id=?", Integer.class, user)).isZero();
        }
    }

    @Test
    void canonicalReplayKeepsStoredSnapshotAfterLaterStateChangesAndClosure() throws Exception {
        UUID user = user();
        UUID session = session(user);
        UUID word = word(0);
        var original = request(session, word, 5);
        JsonNode first = ok(submit(user, original));
        ok(submit(user, request(session, word, 1)));
        ok(call(user, complete(session), event()));
        var before = state(user);
        var reordered = new LinkedHashMap<String, Object>();
        original.entrySet().stream().sorted(Map.Entry.<String, Object>comparingByKey().reversed())
                .forEach(e -> reordered.put(e.getKey(), e.getValue() instanceof UUID ? e.getValue().toString().toUpperCase(java.util.Locale.ROOT) : e.getValue()));
        assertThat(ok(submit(user, reordered))).isEqualTo(first);
        assertThat(state(user)).isEqualTo(before);
        assertSnapshot(original, first);
        assertThat(first.get("progress").get("incorrectCount").asInt()).isZero();
        assertThat(jdbc.queryForObject("SELECT incorrect_count FROM user_vocabulary_progress WHERE user_id=?", Integer.class, user)).isEqualTo(1);
        assertThat(xp(user)).isEqualTo(25);
    }

    @Test
    void reusedPayloadQueryEndpointPathAndUserPreserveAllCommittedState() throws Exception {
        UUID owner = user();
        UUID other = user();
        UUID session = session(owner);
        var original = request(session, word(0), 5);
        JsonNode first = ok(submit(owner, original));
        var ownerBefore = state(owner);
        var otherBefore = state(other);
        var changed = new LinkedHashMap<>(original);
        changed.put("responseTimeMs", 3001);
        error(submit(owner, changed), "IDEMPOTENCY_KEY_REUSE");
        error(call(owner, ATTEMPT + "?source=other", original), "IDEMPOTENCY_KEY_REUSE");
        error(call(owner, "/api/v1/learning/sessions", Map.of("eventId", original.get("eventId"), "sessionType", "MIXED")), "IDEMPOTENCY_KEY_REUSE");
        error(submit(other, original), "IDEMPOTENCY_KEY_REUSE");
        assertThat(state(owner)).isEqualTo(ownerBefore);
        assertThat(state(other)).isEqualTo(otherBefore);
        var completion = event();
        ok(call(owner, complete(session), completion));
        UUID another = session(owner);
        ownerBefore = state(owner);
        error(call(owner, complete(another), completion), "IDEMPOTENCY_KEY_REUSE");
        assertThat(state(owner)).isEqualTo(ownerBefore);
        assertThat(state(other)).isEqualTo(otherBefore);
        assertSnapshot(original, first);
    }

    @Test
    void fourContendersAllReachRealInsertClaimAndReplayOneAtomicMutation() throws Exception {
        UUID user = user();
        UUID session = session(user);
        var request = request(session, word(0), 5);
        List<Boolean> outcomes = contendClaims(4);
        var results = race(List.of(() -> submit(user, request), () -> submit(user, request),
                () -> submit(user, request), () -> submit(user, request)));
        assertThat(outcomes).hasSize(4).containsExactlyInAnyOrder(true, false, false, false);
        JsonNode winner = ok(results.getFirst());
        for (var result : results) assertThat(ok(result)).isEqualTo(winner);
        assertSingleAttempt(user, session, request, winner);
        var before = state(user);
        assertThat(ok(submit(user, request))).isEqualTo(winner);
        assertThat(state(user)).isEqualTo(before);
    }

    @Test
    void concurrentDifferentPayloadCannotReplaceWinningClaimOrSnapshot() throws Exception {
        UUID user = user();
        UUID session = session(user);
        var one = request(session, word(0), 5);
        var two = new LinkedHashMap<>(one);
        two.put("answerQuality", 1);
        contendClaims(2);
        var results = race(List.of(() -> submit(user, one), () -> submit(user, two)));
        assertThat(results.stream().map(MockHttpServletResponse::getStatus).toList()).containsExactlyInAnyOrder(200, 409);
        int winner = results.getFirst().getStatus() == 200 ? 0 : 1;
        var winnerBody = winner == 0 ? one : two;
        var loserBody = winner == 0 ? two : one;
        JsonNode accepted = ok(results.get(winner));
        error(results.get(1 - winner), "IDEMPOTENCY_KEY_REUSE");
        assertThat(accepted.get("answerQuality").asInt()).isEqualTo(winnerBody.get("answerQuality"));
        assertThat(count("session_attempts", user)).isEqualTo(1);
        assertThat(xp(user)).isEqualTo(winner == 0 ? 15 : 10);
        assertSnapshot(winnerBody, accepted);
        var before = state(user);
        error(submit(user, loserBody), "IDEMPOTENCY_KEY_REUSE");
        assertThat(state(user)).isEqualTo(before);
    }

    @Test
    void concurrentCrossUserReuseHasOneOwnerAndNoLosingUserEffects() throws Exception {
        UUID one = user();
        UUID two = user();
        var first = request(session(one), word(0), 5);
        var second = request(session(two), word(0), 5);
        second.put("eventId", first.get("eventId"));
        var beforeOne = state(one);
        var beforeTwo = state(two);
        contendClaims(2);
        var results = race(List.of(() -> submit(one, first), () -> submit(two, second)));
        assertThat(results.stream().map(MockHttpServletResponse::getStatus).toList()).containsExactlyInAnyOrder(200, 409);
        int winner = results.getFirst().getStatus() == 200 ? 0 : 1;
        UUID loserUser = winner == 0 ? two : one;
        assertThat(state(loserUser)).isEqualTo(winner == 0 ? beforeTwo : beforeOne);
        error(results.get(1 - winner), "IDEMPOTENCY_KEY_REUSE");
        var body = winner == 0 ? first : second;
        UUID winnerUser = winner == 0 ? one : two;
        assertSingleAttempt(winnerUser, (UUID) body.get("sessionId"), body, ok(results.get(winner)));
        assertThat(jdbc.queryForObject("SELECT user_id FROM idempotency_keys WHERE event_id=?", UUID.class, first.get("eventId"))).isEqualTo(winnerUser);
    }

    @Test
    void failedClaimantRollsBackLateEffectsAndWaitingDuplicateCanTakeOver() throws Exception {
        UUID user = user();
        UUID session = session(user);
        var request = request(session, word(0), 5);
        List<Boolean> inserted = contendClaims(2);
        AtomicBoolean failFirst = new AtomicBoolean(true);
        List<UUID> rolledBackAttempts = new CopyOnWriteArrayList<>();
        doAnswer(invocation -> {
            invocation.callRealMethod();
            if (failFirst.getAndSet(false)) {
                rolledBackAttempts.add(UUID.fromString(mapper.readTree((String) invocation.getArgument(2)).get("attemptId").asString()));
                throw new IllegalStateException("QA injected failure after response snapshot");
            }
            return null;
        }).when(claims).storeResponse(any(UUID.class), anyInt(), anyString());
        var results = race(List.of(() -> submit(user, request), () -> submit(user, request)));
        assertThat(results.stream().map(MockHttpServletResponse::getStatus).toList()).containsExactlyInAnyOrder(500, 200);
        assertThat(inserted).containsExactly(true, true);
        assertThat(rolledBackAttempts).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM session_attempts WHERE id=?", Integer.class, rolledBackAttempts.getFirst())).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE source_event_id=?", Integer.class, rolledBackAttempts.getFirst())).isZero();
        JsonNode accepted = ok(results.stream().filter(r -> r.getStatus() == 200).findFirst().orElseThrow());
        assertSingleAttempt(user, session, request, accepted);
        reset(claims);
        var before = state(user);
        assertThat(ok(submit(user, request))).isEqualTo(accepted);
        assertThat(state(user)).isEqualTo(before);
    }

    @Test
    void staleProgressHttpConflictRemovesLoserClaimAndExplicitSameEventRetrySucceeds() throws Exception {
        UUID user = user();
        UUID word = word(0);
        ok(submit(user, request(session(user), word, 5)));
        var one = request(session(user), word, 5);
        var two = request(session(user), word, 5);
        CyclicBarrier loaded = new CyclicBarrier(2);
        doAnswer(invocation -> {
            Object value = invocation.callRealMethod();
            loaded.await(10, TimeUnit.SECONDS);
            return value;
        }).when(attempts).loadOrInitialize(user, word, START);
        var results = race(List.of(() -> submit(user, one), () -> submit(user, two)));
        reset(attempts);
        assertThat(results.stream().map(MockHttpServletResponse::getStatus).toList()).containsExactlyInAnyOrder(200, 409);
        int loser = results.getFirst().getStatus() == 409 ? 0 : 1;
        var loserBody = loser == 0 ? one : two;
        var winnerBody = loser == 0 ? two : one;
        error(results.get(loser), "CONCURRENT_UPDATE");
        assertNoClaimOrAttempt(loserBody);
        assertThat(count("session_attempts", user)).isEqualTo(2);
        assertThat(count("xp_logs", user)).isEqualTo(3);
        assertThat(xp(user)).isEqualTo(20);
        assertThat(jdbc.queryForObject("SELECT correct_count FROM user_vocabulary_progress WHERE user_id=?", Integer.class, user)).isEqualTo(2);
        assertSnapshot(winnerBody, ok(results.get(1 - loser)));
        var emptySession = jdbc.queryForMap("SELECT words_studied_count,accuracy_percent,ended_at FROM learning_sessions WHERE id=?", loserBody.get("sessionId"));
        assertThat(emptySession.get("words_studied_count")).isEqualTo(0);
        assertThat(emptySession.get("accuracy_percent")).isNull();
        JsonNode retried = ok(submit(user, loserBody));
        assertThat(retried.get("progress").get("correctCount").asInt()).isEqualTo(3);
        assertThat(xp(user)).isEqualTo(25);
        var before = state(user);
        assertThat(ok(submit(user, loserBody))).isEqualTo(retried);
        assertThat(state(user)).isEqualTo(before);
    }

    @Test
    void staleStreakRollsBackAlreadyWrittenXpProgressAndAttemptBeforeExplicitRetry() throws Exception {
        UUID user = user();
        ok(submit(user, request(session(user), word(0), 5)));
        clock.now = START.plusSeconds(86400);
        var one = request(session(user), word(1), 5);
        var two = request(session(user), word(2), 5);
        CyclicBarrier loaded = new CyclicBarrier(2);
        doAnswer(invocation -> {
            Object value = invocation.callRealMethod();
            loaded.await(10, TimeUnit.SECONDS);
            return value;
        }).when(streaks).findByUserId(user);
        var results = race(List.of(() -> submit(user, one), () -> submit(user, two)));
        reset(streaks);
        assertThat(results.stream().map(MockHttpServletResponse::getStatus).toList()).containsExactlyInAnyOrder(200, 409);
        int loser = results.getFirst().getStatus() == 409 ? 0 : 1;
        var loserBody = loser == 0 ? one : two;
        error(results.get(loser), "CONCURRENT_UPDATE");
        assertNoClaimOrAttempt(loserBody);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?", Integer.class, user, loserBody.get("vocabularyId"))).isZero();
        assertThat(count("session_attempts", user)).isEqualTo(2);
        assertThat(xp(user)).isEqualTo(30);
        assertThat(jdbc.queryForObject("SELECT current_streak FROM streaks WHERE user_id=?", Integer.class, user)).isEqualTo(2);
        ok(submit(user, loserBody));
        assertThat(xp(user)).isEqualTo(35);
        assertThat(count("session_attempts", user)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT current_streak FROM streaks WHERE user_id=?", Integer.class, user)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=? AND reason='STREAK_MAINTAINED'", Integer.class, user)).isEqualTo(2);
    }

    private List<Boolean> contendClaims(int contenders) {
        CyclicBarrier beforeInsert = new CyclicBarrier(contenders);
        List<Boolean> inserted = new CopyOnWriteArrayList<>();
        // Barrier trước INSERT buộc mọi request đã SELECT miss, không thay kết quả SQL.
        doAnswer(invocation -> {
            beforeInsert.await(10, TimeUnit.SECONDS);
            boolean value = (boolean) invocation.callRealMethod();
            inserted.add(value);
            return value;
        }).when(claims).claim(any(UUID.class), any(UUID.class), anyString(), anyString());
        return inserted;
    }
    private List<MockHttpServletResponse> race(List<Callable<MockHttpServletResponse>> calls) throws Exception {
        try (var pool = Executors.newFixedThreadPool(calls.size())) {
            var futures = pool.invokeAll(calls, 45, TimeUnit.SECONDS);
            var results = new ArrayList<MockHttpServletResponse>();
            for (var future : futures) results.add(future.get(5, TimeUnit.SECONDS));
            return results;
        }
    }
    private MockHttpServletResponse submit(UUID user, Map<String, Object> body) throws Exception { return call(user, ATTEMPT, body); }
    private MockHttpServletResponse call(UUID user, String path, Object body) throws Exception {
        return mvc.perform(post(path).header("Authorization", "Bearer " + tokens.issue(user, UserRole.USER))
                .contentType("application/json").content(mapper.writeValueAsString(body))).andReturn().getResponse();
    }
    private JsonNode ok(MockHttpServletResponse response) throws Exception {
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        return mapper.readTree(response.getContentAsString());
    }
    private void error(MockHttpServletResponse response, String code) throws Exception {
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(409);
        assertThat(mapper.readTree(response.getContentAsString()).get("code").asString()).isEqualTo(code);
    }
    private void assertSnapshot(Map<String, Object> request, JsonNode body) {
        var row = jdbc.queryForMap("SELECT response_status,response_snapshot::text FROM idempotency_keys WHERE event_id=?", request.get("eventId"));
        assertThat(row.get("response_status")).isEqualTo(200);
        assertThat(mapper.readTree((String) row.get("response_snapshot"))).isEqualTo(body);
    }
    private void assertSingleAttempt(UUID user, UUID session, Map<String, Object> request, JsonNode response) {
        assertThat(count("session_attempts", user)).isEqualTo(1);
        assertThat(count("user_vocabulary_progress", user)).isEqualTo(1);
        assertThat(count("streaks", user)).isEqualTo(1);
        assertThat(count("idempotency_keys", user)).isEqualTo(1);
        assertThat(xp(user)).isEqualTo(15);
        UUID attempt = UUID.fromString(response.get("attemptId").asString());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=? AND reason='LEARNING_CORRECT' AND xp_amount=5 AND source_event_id=? AND reference_type='session_attempts' AND reference_id=?", Integer.class, user, attempt, attempt)).isEqualTo(1);
        UUID streakSource = UUID.nameUUIDFromBytes(("gamification-v1:STREAK_MAINTAINED:" + user + ":2026-10-18").getBytes(StandardCharsets.UTF_8));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs x JOIN streaks s ON s.id=x.reference_id WHERE x.user_id=? AND x.reason='STREAK_MAINTAINED' AND x.xp_amount=10 AND x.source_event_id=? AND x.reference_type='streaks'", Integer.class, user, streakSource)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT words_studied_count FROM learning_sessions WHERE id=?", Integer.class, session)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT correct_count FROM user_vocabulary_progress WHERE user_id=?", Integer.class, user)).isEqualTo(1);
        assertSnapshot(request, response);
    }
    private void assertNoClaimOrAttempt(Map<String, Object> request) {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM idempotency_keys WHERE event_id=?", Integer.class, request.get("eventId"))).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM session_attempts WHERE session_id=?", Integer.class, request.get("sessionId"))).isZero();
    }
    private UUID user() {
        User user = users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Người học QA", passwords));
        UUID id = user.getId();
        ownedUsers.add(id);
        jdbc.update("INSERT INTO user_profiles(id,user_id,timezone,daily_learning_minutes,created_at,updated_at) VALUES (?,?,'Asia/Ho_Chi_Minh',15,?,?)", UUID.randomUUID(), id, Timestamp.from(START), Timestamp.from(START));
        return id;
    }
    private UUID session(UUID user) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO learning_sessions(id,user_id,session_type,started_at,words_studied_count) VALUES (?,?,'MIXED',?,0)", id, user, Timestamp.from(clock.now));
        return id;
    }
    private UUID word(int offset) { return jdbc.queryForObject("SELECT id FROM vocabulary WHERE is_active=true ORDER BY id LIMIT 1 OFFSET ?", UUID.class, offset); }
    private Map<String, Object> request(UUID session, UUID word, int quality) {
        return new LinkedHashMap<>(Map.of("eventId", UUID.randomUUID(), "sessionId", session, "vocabularyId", word,
                "attemptType", "FLASHCARD", "answerQuality", quality, "responseTimeMs", 3000));
    }
    private Map<String, Object> event() { return Map.of("eventId", UUID.randomUUID()); }
    private String complete(UUID session) { return "/api/v1/learning/sessions/" + session + "/complete"; }
    private long xp(UUID user) { return jdbc.queryForObject("SELECT coalesce(sum(xp_amount),0) FROM xp_logs WHERE user_id=?", Long.class, user); }
    private long count(String table, UUID user) {
        if (table.equals("session_attempts")) return jdbc.queryForObject("SELECT count(*) FROM session_attempts a JOIN learning_sessions s ON s.id=a.session_id WHERE s.user_id=?", Long.class, user);
        return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE user_id=?", Long.class, user);
    }
    private Map<String, Object> state(UUID user) {
        var result = new LinkedHashMap<String, Object>();
        for (String table : List.of("learning_sessions", "user_vocabulary_progress", "xp_logs", "streaks"))
            result.put(table, jdbc.queryForList("SELECT * FROM " + table + " WHERE user_id=? ORDER BY id", user));
        result.put("session_attempts", jdbc.queryForList("SELECT a.* FROM session_attempts a JOIN learning_sessions s ON s.id=a.session_id WHERE s.user_id=? ORDER BY a.id", user));
        result.put("idempotency_keys", jdbc.queryForList("SELECT * FROM idempotency_keys WHERE user_id=? ORDER BY event_id", user));
        return result;
    }
    private static String secret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    @TestConfiguration
    static class TimeConfiguration {
        @Bean @Primary MutableClock clock() { return new MutableClock(); }
    }
    static final class MutableClock extends Clock {
        private volatile Instant now = START;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(now, zone); }
        @Override public Instant instant() { return now; }
    }
}
