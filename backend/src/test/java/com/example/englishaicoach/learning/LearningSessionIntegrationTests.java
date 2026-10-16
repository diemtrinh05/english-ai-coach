package com.example.englishaicoach.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(LearningSessionIntegrationTests.TimeConfiguration.class)
class LearningSessionIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-13T08:15:30Z");
    private static final String SECRET = randomSecret();

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService access;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> SECRET);
    }

    @Test
    void startsAndReadsOwnedSessionWithCanonicalInitialAggregates() throws Exception {
        User user = user();
        String token = token(user, UserRole.USER);
        for (LearningSessionType type : LearningSessionType.values()) {
            var result = start(token, Map.of("sessionType", type.name(), "eventId", UUID.randomUUID()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sessionType").value(type.name()))
                    .andExpect(jsonPath("$.startedAt").value(NOW.toString()))
                    .andExpect(jsonPath("$.wordsStudiedCount").value(0))
                    .andReturn().getResponse();
            var body = mapper.readTree(result.getContentAsString());
            UUID sessionId = UUID.fromString(body.get("sessionId").asText());
            assertThat(body.get("endedAt").isNull()).isTrue();
            assertThat(body.get("accuracyPercent").isNull()).isTrue();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE id=? AND user_id=?",
                    Integer.class, sessionId, user.getId())).isEqualTo(1);
            mvc.perform(get("/api/v1/learning/sessions/{sessionId}", sessionId)
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                    .andExpect(jsonPath("$.sessionType").value(type.name()));
        }
    }

    @Test
    void retryReplaysSnapshotAndReuseRejectsDifferentRequestOrUser() throws Exception {
        User owner = user();
        User other = user();
        String ownerToken = token(owner, UserRole.USER);
        UUID eventId = UUID.randomUUID();
        Map<String, Object> request = Map.of("sessionType", "REVIEW", "eventId", eventId);
        String first = start(ownerToken, request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String replay = start(ownerToken, request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(replay)).isEqualTo(mapper.readTree(first));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, owner.getId())).isEqualTo(1);
        start(ownerToken, Map.of("sessionType", "MIXED", "eventId", eventId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        start(token(other, UserRole.USER), request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, other.getId())).isZero();
    }

    @Test
    void unknownBodyPropertyIsRejectedBeforeClaimAndCannotReplayExistingEvent() throws Exception {
        User user = user();
        String token = token(user, UserRole.USER);
        UUID freshEvent = UUID.randomUUID();
        start(token, Map.of("sessionType", "REVIEW", "eventId", freshEvent, "extra", "giá trị khác"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, user.getId())).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM idempotency_keys WHERE event_id=?",
                Integer.class, freshEvent)).isZero();

        UUID existingEvent = UUID.randomUUID();
        start(token, Map.of("sessionType", "REVIEW", "eventId", existingEvent))
                .andExpect(status().isCreated());
        start(token, Map.of("sessionType", "REVIEW", "eventId", existingEvent, "extra", "giá trị khác"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, user.getId())).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM idempotency_keys WHERE event_id=?",
                Integer.class, existingEvent)).isEqualTo(1);
        start(token, Map.of("sessionType", "MIXED", "eventId", existingEvent))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
    }

    @Test
    void numericDeclaredFieldsAreRejectedBeforeClaimAndCannotReplayExistingEvent() throws Exception {
        User user = user();
        String token = token(user, UserRole.USER);
        UUID freshEvent = UUID.randomUUID();
        start(token, Map.of("sessionType", 0, "eventId", freshEvent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        start(token, Map.of("sessionType", "MIXED", "eventId", 123))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, user.getId())).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM idempotency_keys WHERE event_id=?",
                Integer.class, freshEvent)).isZero();

        UUID existingEvent = UUID.randomUUID();
        Map<String, Object> valid = Map.of("sessionType", "REVIEW", "eventId", existingEvent);
        String first = start(token, valid).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        start(token, Map.of("sessionType", 0, "eventId", existingEvent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, user.getId())).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM idempotency_keys WHERE event_id=?",
                Integer.class, existingEvent)).isEqualTo(1);
        String replay = start(token, valid).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(replay)).isEqualTo(mapper.readTree(first));
        start(token, Map.of("sessionType", "MIXED", "eventId", existingEvent))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
    }

    @Test
    void missingForeignAndUnauthenticatedReadsDoNotRevealSession() throws Exception {
        User owner = user();
        User other = user();
        String ownerToken = token(owner, UserRole.USER);
        UUID sessionId = UUID.fromString(mapper.readTree(start(ownerToken,
                Map.of("sessionType", "NEW_WORDS", "eventId", UUID.randomUUID()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
                .get("sessionId").asText());
        mvc.perform(get("/api/v1/learning/sessions/{sessionId}", sessionId)
                .header("Authorization", "Bearer " + token(other, UserRole.USER)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(get("/api/v1/learning/sessions/{sessionId}", UUID.randomUUID())
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(get("/api/v1/learning/sessions/{sessionId}", sessionId))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/learning/sessions/{sessionId}", sessionId)
                .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRoleAndInputValidationApplyBeforeMutation() throws Exception {
        User user = user();
        String userToken = token(user, UserRole.USER);
        String adminToken = token(user, UserRole.ADMIN);
        start(adminToken, Map.of("sessionType", "MIXED", "eventId", UUID.randomUUID()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(get("/api/v1/learning/sessions/{sessionId}", UUID.randomUUID())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
        for (Map<String, Object> request : new Map[] {
                Map.of("sessionType", "MIXED"), Map.of("eventId", UUID.randomUUID()),
                Map.of("sessionType", "SPEAKING", "eventId", UUID.randomUUID()),
                Map.of("sessionType", "MIXED", "eventId", "not-a-uuid") }) {
            start(userToken, request).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        start(userToken, Map.of("sessionType", "MIXED", "eventId", UUID.randomUUID()))
                .andExpect(status().isCreated());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, user.getId())).isEqualTo(1);
        start(null, Map.of("sessionType", "MIXED", "eventId", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
        start(token(UUID.randomUUID(), UserRole.USER), Map.of("sessionType", "MIXED", "eventId", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void concurrentSameEventCreatesOnlyOneSessionAndReplays() throws Exception {
        User user = user();
        String token = token(user, UserRole.USER);
        Map<String, Object> request = Map.of("sessionType", "MIXED", "eventId", UUID.randomUUID());
        CyclicBarrier barrier = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            ArrayList<Callable<String>> calls = new ArrayList<>();
            for (int index = 0; index < 2; index++) {
                calls.add(() -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return start(token, request).andExpect(status().isCreated())
                            .andReturn().getResponse().getContentAsString();
                });
            }
            var results = pool.invokeAll(calls);
            assertThat(mapper.readTree(results.get(0).get(30, TimeUnit.SECONDS)))
                    .isEqualTo(mapper.readTree(results.get(1).get(30, TimeUnit.SECONDS)));
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, user.getId())).isEqualTo(1);
    }

    @Test
    void sessionReadReflectsBackendAggregatesAndQueryIsPartOfIdempotencyIdentity() throws Exception {
        User user = user();
        String token = token(user, UserRole.USER);
        UUID eventId = UUID.randomUUID();
        Map<String, Object> request = Map.of("sessionType", "REVIEW", "eventId", eventId);
        String body = mvc.perform(post("/api/v1/learning/sessions?source=study")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json").content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID sessionId = UUID.fromString(mapper.readTree(body).get("sessionId").asText());
        mvc.perform(post("/api/v1/learning/sessions?source=review")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json").content(mapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        jdbc.update("""
                UPDATE learning_sessions SET ended_at=?,words_studied_count=2,accuracy_percent=50.00
                WHERE id=?
                """, java.sql.Timestamp.from(NOW.plusSeconds(120)), sessionId);
        mvc.perform(get("/api/v1/learning/sessions/{sessionId}", sessionId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endedAt").value(NOW.plusSeconds(120).toString()))
                .andExpect(jsonPath("$.wordsStudiedCount").value(2))
                .andExpect(jsonPath("$.accuracyPercent").value(50.0));
    }

    @Test
    void insertFailureRollsBackClaimAndAllowsSameEventRetry() throws Exception {
        User user = user();
        String token = token(user, UserRole.USER);
        UUID eventId = UUID.randomUUID();
        Map<String, Object> request = Map.of("sessionType", "MIXED", "eventId", eventId);
        jdbc.execute("CREATE FUNCTION reject_session_test() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$");
        jdbc.execute("CREATE TRIGGER reject_session_test BEFORE INSERT ON learning_sessions FOR EACH ROW EXECUTE FUNCTION reject_session_test()");
        try {
            start(token, request).andExpect(status().isInternalServerError());
            assertThat(jdbc.queryForObject("SELECT count(*) FROM idempotency_keys WHERE event_id=?",
                    Integer.class, eventId)).isZero();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                    Integer.class, user.getId())).isZero();
        } finally {
            jdbc.execute("DROP TRIGGER reject_session_test ON learning_sessions");
            jdbc.execute("DROP FUNCTION reject_session_test()");
        }
        start(token, request).andExpect(status().isCreated());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM learning_sessions WHERE user_id=?",
                Integer.class, user.getId())).isEqualTo(1);
    }

    private org.springframework.test.web.servlet.ResultActions start(String token, Map<String, ?> body) throws Exception {
        var builder = post("/api/v1/learning/sessions").contentType("application/json")
                .content(mapper.writeValueAsString(body));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(builder);
    }

    private User user() {
        return users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test",
                "password", "Người học", passwords));
    }

    private String token(User user, UserRole role) {
        return token(user.getId(), role);
    }

    private String token(UUID userId, UserRole role) {
        return access.issue(userId, role);
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @TestConfiguration
    static class TimeConfiguration {
        @Bean @Primary Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
