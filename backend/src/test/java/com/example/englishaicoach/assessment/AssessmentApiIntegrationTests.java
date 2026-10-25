package com.example.englishaicoach.assessment;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(AssessmentApiIntegrationTests.TimeConfiguration.class)
class AssessmentApiIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-24T08:00:00Z");
    private static final String SECRET = randomSecret();
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService access;

    @DynamicPropertySource static void config(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> SECRET);
    }

    @ParameterizedTest @EnumSource(AssessmentType.class)
    void startsEveryCanonicalTypeWithoutProfileAndPersistsFirstSnapshotBeforeResponse(AssessmentType type) throws Exception {
        User owner = user();
        UUID event = UUID.randomUUID();
        var response = start(token(owner), Map.of("type", type.name(), "eventId", event))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.type").value(type.name()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentDifficulty").value("A1")).andReturn().getResponse();
        JsonNode body = mapper.readTree(response.getContentAsString());
        assertThat(body.size()).isEqualTo(4);
        UUID id = UUID.fromString(body.get("assessmentId").asText());
        assertThat(count("user_level_assessments", "user_id", owner.getId())).isEqualTo(1);
        assertThat(count("assessment_items", "assessment_id", id)).isEqualTo(1);
        assertThat(count("idempotency_keys", "event_id", event)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT started_at FROM user_level_assessments WHERE id=?",
                java.sql.Timestamp.class, id).toInstant()).isEqualTo(NOW);
        read(token(owner), id, "").andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuestions").value(0)).andExpect(jsonPath("$.correctAnswers").value(0))
                .andExpect(jsonPath("$.createdAt").value(NOW.toString())).andExpect(jsonPath("$.cefrLevel").doesNotExist());
        JsonNode result = mapper.readTree(read(token(owner), id, "").andReturn().getResponse().getContentAsString());
        assertThat(result.has("score")).isTrue();
        assertThat(result.get("score").isNull()).isTrue();
        assertThat(result.size()).isEqualTo(6);
        assertThat(result.has("currentDifficulty")).isFalse();
        JsonNode question = mapper.readTree(read(token(owner), id, "/next-question")
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(question.size()).isEqualTo(4);
        assertThat(question.has("correctAnswer")).isFalse();
        assertThat(question.get("options").size()).isEqualTo(4);
        assertThat(question.get("cefrLevel").asText()).isEqualTo("A1");
        assertThat(jdbc.queryForObject("SELECT id FROM assessment_items WHERE assessment_id=?",
                UUID.class, id).toString()).isEqualTo(question.get("questionId").asText());
    }

    @Test void replaysBeforeActiveAndCatalogGuardsAndAfterClosedAndRejectsReuse() throws Exception {
        User owner = user();
        UUID event = UUID.randomUUID();
        Map<String, Object> payload = Map.of("type", "INITIAL", "eventId", event);
        JsonNode first = mapper.readTree(start(token(owner), payload).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        assertThat(mapper.readTree(start(token(owner), payload).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString())).isEqualTo(first);
        UUID id = UUID.fromString(first.get("assessmentId").asText());
        start(token(owner), Map.of("type", "PERIODIC", "eventId", event))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        start(token(user()), payload).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        jdbc.update("UPDATE user_level_assessments SET status='CANCELLED' WHERE id=?", id);
        List<UUID> active = activeIds();
        jdbc.update("UPDATE vocabulary SET is_active=FALSE WHERE is_active=TRUE");
        try {
            assertThat(mapper.readTree(start(token(owner), payload).andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString())).isEqualTo(first);
        } finally { restoreActive(active); }
        assertThat(count("user_level_assessments", "user_id", owner.getId())).isEqualTo(1);
        assertThat(count("assessment_items", "assessment_id", id)).isEqualTo(1);
    }

    @Test void activeAssessmentConflictPrecedesContentGuardAndRollsBackNewClaim() throws Exception {
        User owner = user();
        startId(owner);
        UUID event = UUID.randomUUID();
        List<UUID> active = activeIds();
        jdbc.update("UPDATE vocabulary SET is_active=FALSE WHERE is_active=TRUE");
        try {
            start(token(owner), Map.of("type", "INITIAL", "eventId", event)).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("ASSESSMENT_ALREADY_IN_PROGRESS"));
        } finally { restoreActive(active); }
        assertThat(count("idempotency_keys", "event_id", event)).isZero();
    }

    @Test void missingContentRollsBackClaimAndCanRetrySameEventAfterRecovery() throws Exception {
        User owner = user();
        UUID event = UUID.randomUUID();
        Map<String, Object> body = Map.of("type", "MANUAL", "eventId", event);
        List<UUID> active = activeIds();
        jdbc.update("UPDATE vocabulary SET is_active=FALSE WHERE is_active=TRUE");
        try {
            start(token(owner), body).andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.code").value("ASSESSMENT_CONTENT_UNAVAILABLE"));
            assertThat(count("user_level_assessments", "user_id", owner.getId())).isZero();
            assertThat(count("idempotency_keys", "event_id", event)).isZero();
        } finally { restoreActive(active); }
        start(token(owner), body).andExpect(status().isCreated());
    }

    @Test void persistenceFailureRollsBackAggregateItemAndClaim() throws Exception {
        User owner = user();
        UUID event = UUID.randomUUID();
        Map<String, Object> body = Map.of("type", "INITIAL", "eventId", event);
        jdbc.execute("CREATE FUNCTION reject_asmt003_item() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$");
        jdbc.execute("CREATE TRIGGER reject_asmt003_item BEFORE INSERT ON assessment_items FOR EACH ROW EXECUTE FUNCTION reject_asmt003_item()");
        try {
            start(token(owner), body).andExpect(status().isInternalServerError());
            assertThat(count("user_level_assessments", "user_id", owner.getId())).isZero();
            assertThat(count("idempotency_keys", "event_id", event)).isZero();
        } finally {
            jdbc.execute("DROP TRIGGER reject_asmt003_item ON assessment_items");
            jdbc.execute("DROP FUNCTION reject_asmt003_item()");
        }
        start(token(owner), body).andExpect(status().isCreated());
    }

    @Test void nextReadsSavedRawOptionsAndQuestionAfterCatalogChangesWithoutWriting() throws Exception {
        User owner = user();
        UUID id = startId(owner);
        UUID target = jdbc.queryForObject("SELECT vocabulary_id FROM assessment_items WHERE assessment_id=?", UUID.class, id);
        var catalog = jdbc.queryForMap("SELECT word,meaning_vi,is_active FROM vocabulary WHERE id=?", target);
        String before = databaseSnapshot(id);
        JsonNode question = mapper.readTree(read(token(owner), id, "/next-question").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        jdbc.update("UPDATE vocabulary SET is_active=FALSE,word='changed',meaning_vi='changed' WHERE id=?", target);
        try {
            for (int i = 0; i < 3; i++) {
                assertThat(mapper.readTree(read(token(owner), id, "/next-question").andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString())).isEqualTo(question);
                read(token(owner), id, "").andExpect(status().isOk());
            }
            assertThat(databaseSnapshot(id)).isEqualTo(before);
        } finally {
            jdbc.update("UPDATE vocabulary SET word=?,meaning_vi=?,is_active=? WHERE id=?",
                    catalog.get("word"), catalog.get("meaning_vi"), catalog.get("is_active"), target);
        }
    }

    @ParameterizedTest @ValueSource(strings = {"COMPLETED", "CANCELLED"})
    void closedNextIs404AndResultUsesStoredFields(String state) throws Exception {
        User owner = user();
        UUID id = startId(owner);
        jdbc.update("""
                UPDATE user_level_assessments SET status=?,score=73.21,questions_answered=27,
                correct_answers=19,final_cefr_level_id=(SELECT id FROM cefr_levels WHERE code='B2') WHERE id=?
                """, state, id);
        read(token(owner), id, "/next-question").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        var result = read(token(owner), id, "").andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(73.21)).andExpect(jsonPath("$.totalQuestions").value(27))
                .andExpect(jsonPath("$.correctAnswers").value(19)).andExpect(jsonPath("$.status").value(state));
        if (state.equals("COMPLETED")) result.andExpect(jsonPath("$.cefrLevel.code").value("B2"));
        else result.andExpect(jsonPath("$.cefrLevel").doesNotExist());
    }

    @Test void nextMissingPersistedUnansweredDoesNotGenerateAndReturns503() throws Exception {
        User owner = user();
        UUID id = startId(owner);
        jdbc.update("UPDATE assessment_items SET answered_at=? WHERE assessment_id=?", java.sql.Timestamp.from(NOW), id);
        String before = databaseSnapshot(id);
        read(token(owner), id, "/next-question").andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("ASSESSMENT_CONTENT_UNAVAILABLE"));
        assertThat(databaseSnapshot(id)).isEqualTo(before);
        assertThat(count("assessment_items", "assessment_id", id)).isEqualTo(1);
    }

    @ParameterizedTest @ValueSource(strings = {"COMPLETED", "CANCELLED"})
    void nullableStoredScoreAndAbsentFinalCefrAreNotInvented(String state) throws Exception {
        User owner = user();
        UUID id = startId(owner);
        jdbc.update("UPDATE user_level_assessments SET status=? WHERE id=?", state, id);
        JsonNode result = mapper.readTree(read(token(owner), id, "").andExpect(status().isOk())
                .andExpect(jsonPath("$.cefrLevel").doesNotExist()).andReturn().getResponse().getContentAsString());
        assertThat(result.has("score")).isTrue();
        assertThat(result.get("score").isNull()).isTrue();
    }

    @Test void nextSelectsFirstUnansweredByPersistedSequenceAndKeepsRawText() throws Exception {
        User owner = user();
        UUID id = startId(owner);
        jdbc.update("UPDATE assessment_items SET answered_at=? WHERE assessment_id=?", java.sql.Timestamp.from(NOW), id);
        UUID a1 = jdbc.queryForObject("SELECT id FROM cefr_levels WHERE code='A1'", UUID.class);
        List<UUID> targets = jdbc.query("""
                SELECT id FROM vocabulary WHERE id NOT IN
                (SELECT vocabulary_id FROM assessment_items WHERE assessment_id=?) ORDER BY id LIMIT 2
                """, (rs, n) -> rs.getObject(1, UUID.class), id);
        UUID second = UUID.randomUUID();
        String options = mapper.writeValueAsString(List.of(" a ", "b\n", "c", "đ"));
        for (int sequence : List.of(3, 2)) {
            jdbc.update("""
                    INSERT INTO assessment_items(id,assessment_id,sequence_no,vocabulary_id,cefr_level_id,
                    question_text,options_json,correct_answer,created_at) VALUES (?,?,?,?,?,?,?::jsonb,?,?)
                    """, sequence == 2 ? second : UUID.randomUUID(), id, sequence, targets.get(sequence - 2),
                    a1, "  câu hỏi " + sequence + "  ", options, " a ", java.sql.Timestamp.from(NOW));
        }
        String before = databaseSnapshot(id);
        var response = read(token(owner), id, "/next-question").andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(second.toString()))
                .andExpect(jsonPath("$.questionText").value("  câu hỏi 2  ")).andReturn().getResponse();
        JsonNode result = mapper.readTree(response.getContentAsString());
        assertThat(result.get("options")).isEqualTo(mapper.readTree(options));
        assertThat(databaseSnapshot(id)).isEqualTo(before);
    }

    @Test void ownershipAuthenticationAndMissingUserAreEnforcedForAllRoutes() throws Exception {
        User owner = user();
        UUID id = startId(owner);
        for (String suffix : List.of("", "/next-question")) {
            read(token(user()), id, suffix).andExpect(status().isNotFound());
            read(token(owner), UUID.randomUUID(), suffix).andExpect(status().isNotFound());
            read(null, id, suffix).andExpect(status().isUnauthorized());
            read("invalid", id, suffix).andExpect(status().isUnauthorized());
            read(access.issue(owner.getId(), UserRole.ADMIN), id, suffix).andExpect(status().isForbidden());
            read(access.issue(UUID.randomUUID(), UserRole.USER), id, suffix).andExpect(status().isUnauthorized());
        }
        Map<String, Object> body = Map.of("type", "INITIAL", "eventId", UUID.randomUUID());
        start(null, body).andExpect(status().isUnauthorized());
        start("invalid", body).andExpect(status().isUnauthorized());
        start(access.issue(owner.getId(), UserRole.ADMIN), body).andExpect(status().isForbidden());
        start(access.issue(UUID.randomUUID(), UserRole.USER), body).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest @ValueSource(strings = {"{}", "null", "[]", "{\"type\":\"INITIAL\"}",
            "{\"type\":null,\"eventId\":\"00000000-0000-0000-0000-000000000000\"}",
            "{\"type\":1,\"eventId\":\"00000000-0000-0000-0000-000000000000\"}",
            "{\"type\":\"OTHER\",\"eventId\":\"00000000-0000-0000-0000-000000000000\"}",
            "{\"type\":\"INITIAL\",\"eventId\":1}",
            "{\"type\":\"INITIAL\",\"eventId\":\"1-1-1-1-1\"}",
            "{\"type\":\"INITIAL\",\"eventId\":\"00000000-0000-0000-0000-000000000000\",\"extra\":1}",
            "{\"type\":\"INITIAL\",\"type\":\"MANUAL\",\"eventId\":\"00000000-0000-0000-0000-000000000000\"}"})
    void rejectsMalformedBodiesBeforeClaim(String raw) throws Exception {
        User owner = user();
        mvc.perform(post(AssessmentService.ROUTE).header("Authorization", "Bearer " + token(owner))
                .contentType("application/json").content(raw)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(count("user_level_assessments", "user_id", owner.getId())).isZero();
        assertThat(count("idempotency_keys", "user_id", owner.getId())).isZero();
    }

    @Test void rejectsAnyQueryAndMalformedPathUuidBeforeReadsOrClaims() throws Exception {
        User owner = user();
        String auth = "Bearer " + token(owner);
        mvc.perform(post(AssessmentService.ROUTE + "?unknown=1&unknown=2").header("Authorization", auth)
                .contentType("application/json").content(mapper.writeValueAsString(
                        Map.of("type", "INITIAL", "eventId", UUID.randomUUID())))).andExpect(status().isBadRequest());
        for (String suffix : List.of("", "/next-question")) {
            mvc.perform(get(AssessmentService.ROUTE + "/1-1-1-1-1" + suffix).header("Authorization", auth))
                    .andExpect(status().isBadRequest());
            mvc.perform(get(AssessmentService.ROUTE + "/" + UUID.randomUUID() + suffix + "?x=1&x=2")
                    .header("Authorization", auth)).andExpect(status().isBadRequest());
        }
        assertThat(count("idempotency_keys", "user_id", owner.getId())).isZero();
    }

    @Test void sameEventBoundToAnotherEndpointCannotCreateAssessment() throws Exception {
        User owner = user();
        UUID event = UUID.randomUUID();
        mvc.perform(post("/api/v1/learning/sessions").header("Authorization", "Bearer " + token(owner))
                .contentType("application/json").content(mapper.writeValueAsString(
                        Map.of("sessionType", "MIXED", "eventId", event)))).andExpect(status().isCreated());
        start(token(owner), Map.of("type", "INITIAL", "eventId", event)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        assertThat(count("user_level_assessments", "user_id", owner.getId())).isZero();
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void concurrentStartsHaveOneAggregateAndNoUnexpected500(boolean sameEvent) throws Exception {
        User owner = user();
        String token = token(owner);
        UUID shared = UUID.randomUUID();
        CyclicBarrier barrier = new CyclicBarrier(4);
        List<Callable<org.springframework.mock.web.MockHttpServletResponse>> tasks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            UUID event = sameEvent ? shared : UUID.randomUUID();
            tasks.add(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return start(token, Map.of("type", "INITIAL", "eventId", event))
                        .andReturn().getResponse();
            });
        }
        try (var pool = Executors.newFixedThreadPool(4)) {
            List<org.springframework.mock.web.MockHttpServletResponse> responses = new ArrayList<>();
            for (var future : pool.invokeAll(tasks)) responses.add(future.get(30, TimeUnit.SECONDS));
            assertThat(responses.stream().map(r -> r.getStatus()).toList())
                    .containsOnly(sameEvent ? new Integer[] {201} : new Integer[] {201, 409});
            assertThat(responses.stream().filter(r -> r.getStatus() == 201).count()).isEqualTo(sameEvent ? 4 : 1);
            JsonNode first = mapper.readTree(responses.stream().filter(r -> r.getStatus() == 201)
                    .findFirst().orElseThrow().getContentAsString());
            for (var response : responses) {
                JsonNode body = mapper.readTree(response.getContentAsString());
                if (response.getStatus() == 201) assertThat(body).isEqualTo(first);
                else assertThat(body.get("code").asText()).isEqualTo("ASSESSMENT_ALREADY_IN_PROGRESS");
            }
        }
        assertThat(count("user_level_assessments", "user_id", owner.getId())).isEqualTo(1);
        assertThat(count("idempotency_keys", "user_id", owner.getId())).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM assessment_items i JOIN user_level_assessments a ON i.assessment_id=a.id WHERE a.user_id=?",
                Integer.class, owner.getId())).isEqualTo(1);
    }

    private String databaseSnapshot(UUID id) {
        return jdbc.queryForObject("SELECT row_to_json(a)::text FROM user_level_assessments a WHERE id=?", String.class, id)
                + jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(i) ORDER BY sequence_no)::text FROM assessment_items i WHERE assessment_id=?", String.class, id);
    }

    private int count(String table, String field, UUID value) {
        // Tên bảng/cột chỉ do test fixtures hằng số trong lớp này cung cấp.
        return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE " + field + "=?", Integer.class, value);
    }

    private List<UUID> activeIds() {
        return jdbc.query("SELECT id FROM vocabulary WHERE is_active=TRUE", (rs, n) -> rs.getObject(1, UUID.class));
    }

    private void restoreActive(List<UUID> ids) {
        for (UUID id : ids) jdbc.update("UPDATE vocabulary SET is_active=TRUE WHERE id=?", id);
    }

    private UUID startId(User owner) throws Exception {
        return UUID.fromString(mapper.readTree(start(token(owner), Map.of("type", "INITIAL", "eventId", UUID.randomUUID()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("assessmentId").asText());
    }

    private ResultActions start(String token, Map<String, ?> body) throws Exception {
        var request = post(AssessmentService.ROUTE).contentType("application/json").content(mapper.writeValueAsString(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return mvc.perform(request);
    }

    private ResultActions read(String token, UUID id, String suffix) throws Exception {
        var request = get(AssessmentService.ROUTE + "/" + id + suffix);
        if (token != null) request.header("Authorization", "Bearer " + token);
        return mvc.perform(request);
    }

    private User user() { return users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Người học", passwords)); }
    private String token(User user) { return access.issue(user.getId(), UserRole.USER); }
    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @TestConfiguration static class TimeConfiguration {
        @Bean @Primary Clock clock() { return Clock.fixed(NOW, ZoneOffset.UTC); }
    }
}
