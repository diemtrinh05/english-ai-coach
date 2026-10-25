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
@Import(AssessmentAnswerIntegrationTests.TimeConfiguration.class)
class AssessmentAnswerIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-25T08:00:00Z");
    private static final String SECRET = secret();
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService tokens;

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> SECRET);
    }

    @Test void recordsRawCorrectnessCountersTimingAndOneNextSnapshotWithReplay() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        UUID question = question(f.id());
        jdbc.update("UPDATE assessment_items SET correct_answer=?,options_json=CAST(? AS jsonb) WHERE id=?",
                "  Đúng\n", mapper.writeValueAsString(List.of("  Đúng\n", "Sai", "Khác", "Nữa")), question);
        UUID event = UUID.randomUUID();
        var body = payload(question, "  Đúng\n", 1234, event);
        var first = submit(f.token(), f.id(), body).andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true)).andExpect(jsonPath("$.nextDifficulty").value("A1"))
                .andExpect(jsonPath("$.questionsAnswered").value(1)).andExpect(jsonPath("$.isCompleted").value(false));
        JsonNode response = mapper.readTree(first.andReturn().getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(4);
        assertThat(jdbc.queryForMap("SELECT selected_answer,is_correct,response_time_ms,answered_at FROM assessment_items WHERE id=?", question))
                .containsEntry("selected_answer", "  Đúng\n").containsEntry("is_correct", true).containsEntry("response_time_ms", 1234);
        assertThat(jdbc.queryForObject("SELECT answered_at FROM assessment_items WHERE id=?", java.sql.Timestamp.class, question).toInstant()).isEqualTo(NOW);
        assertThat(score(f.id())).isEqualByComparingTo("100.00");
        assertThat(count("assessment_items", "assessment_id", f.id())).isEqualTo(2);
        assertThat(count("idempotency_keys", "event_id", event)).isEqualTo(1);
        String before = snapshot(f.id());
        assertThat(mapper.readTree(submit(f.token(), f.id(), body).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString())).isEqualTo(response);
        assertThat(snapshot(f.id())).isEqualTo(before);
        submit(f.token(), f.id(), payload(question, "Sai", 1234, event)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        submit(tokens.issue(user().getId(), UserRole.USER), f.id(), body).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        mvc.perform(get(route(f.id(), "/next-question")).header("Authorization", "Bearer " + f.token()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correctAnswer").doesNotExist());
    }

    @Test void outsideRawOptionsFailsWithoutMutationAndLowercaseOrTrimDoesNotMatch() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        UUID question = question(f.id());
        jdbc.update("UPDATE assessment_items SET correct_answer=?,options_json=CAST(? AS jsonb) WHERE id=?",
                " ĐÚNG ", mapper.writeValueAsString(List.of(" ĐÚNG ", "SAI", "Khác", "Nữa")), question);
        String before = snapshot(f.id());
        for (String value : List.of("ĐÚNG", " đúng ", "unlisted", "")) {
            UUID event = UUID.randomUUID();
            submit(f.token(), f.id(), payload(question, value, 0, event)).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
            assertThat(count("idempotency_keys", "event_id", event)).isZero();
        }
        assertThat(snapshot(f.id())).isEqualTo(before);
        submit(f.token(), f.id(), payload(question, "SAI", Integer.MAX_VALUE, UUID.randomUUID()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(false));
        assertThat(score(f.id())).isEqualByComparingTo("0.00");
    }

    @ParameterizedTest @EnumSource(AssessmentType.class)
    void completesAtTwentyUpdatesExistingProfileIncludingDemotionForEveryType(AssessmentType type) throws Exception {
        Fixture f = fixture(type);
        UUID c2 = level("C2");
        jdbc.update("INSERT INTO user_profiles(id,user_id,current_cefr_level_id,daily_learning_minutes,timezone,created_at,updated_at) VALUES (?,?,?,25,'Asia/Ho_Chi_Minh',?,?)",
                UUID.randomUUID(), f.owner().getId(), c2, java.sql.Timestamp.from(NOW.minusSeconds(60)), java.sql.Timestamp.from(NOW.minusSeconds(60)));
        for (int n = 1; n <= 20; n++) {
            JsonNode body = accepted(f, false);
            assertThat(body.get("questionsAnswered").asInt()).isEqualTo(n);
            assertThat(body.get("isCompleted").asBoolean()).isEqualTo(n == 20);
            assertThat(body.get("nextDifficulty").asText()).isEqualTo("A1");
            if (n < 20) assertThat(jdbc.queryForObject("SELECT current_cefr_level_id FROM user_profiles WHERE user_id=?", UUID.class, f.owner().getId())).isEqualTo(c2);
        }
        assertThat(count("assessment_items", "assessment_id", f.id())).isEqualTo(20);
        assertThat(jdbc.queryForObject("SELECT status FROM user_level_assessments WHERE id=?", String.class, f.id())).isEqualTo("COMPLETED");
        assertThat(jdbc.queryForObject("SELECT final_cefr_level_id FROM user_level_assessments WHERE id=?", UUID.class, f.id())).isEqualTo(level("A1"));
        assertThat(jdbc.queryForObject("SELECT current_cefr_level_id FROM user_profiles WHERE user_id=?", UUID.class, f.owner().getId())).isEqualTo(level("A1"));
        assertThat(jdbc.queryForObject("SELECT updated_at FROM user_profiles WHERE user_id=?", java.sql.Timestamp.class, f.owner().getId()).toInstant()).isEqualTo(NOW);
        assertThat(jdbc.queryForObject("SELECT daily_learning_minutes FROM user_profiles WHERE user_id=?", Integer.class, f.owner().getId())).isEqualTo(25);
        assertThat(jdbc.queryForObject("SELECT completed_at FROM user_level_assessments WHERE id=?", java.sql.Timestamp.class, f.id()).toInstant()).isEqualTo(NOW);
        assertThat(score(f.id())).isEqualByComparingTo("0.00");
        assertThat(count("xp_logs", "user_id", f.owner().getId())).isZero();
        assertThat(count("streaks", "user_id", f.owner().getId())).isZero();
        assertThat(count("user_vocabulary_progress", "user_id", f.owner().getId())).isZero();
    }

    @Test void completesAtC2WithoutProfileAndLaterProfilePutDoesNotBackfill() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        JsonNode last = null;
        UUID lastQuestion = null;
        Map<String, Object> lastPayload = null;
        for (int n = 1; n <= 28; n++) {
            lastQuestion = question(f.id());
            lastPayload = payload(lastQuestion, correct(lastQuestion), 9, UUID.randomUUID());
            last = mapper.readTree(submit(f.token(), f.id(), lastPayload).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
            assertThat(last.get("isCompleted").asBoolean()).isEqualTo(n == 28);
        }
        assertThat(last.get("nextDifficulty").asText()).isEqualTo("C2");
        assertThat(count("user_profiles", "user_id", f.owner().getId())).isZero();
        assertThat(count("assessment_items", "assessment_id", f.id())).isEqualTo(28);
        assertThat(score(f.id())).isEqualByComparingTo("100.00");
        String before = snapshot(f.id());
        assertThat(mapper.readTree(submit(f.token(), f.id(), lastPayload).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString())).isEqualTo(last);
        submit(f.token(), f.id(), payload(lastQuestion, correct(lastQuestion), 9, UUID.randomUUID()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ASSESSMENT_QUESTION_ALREADY_ANSWERED"));
        assertThat(snapshot(f.id())).isEqualTo(before);
        mvc.perform(get(route(f.id(), "")).header("Authorization", "Bearer " + f.token()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cefrLevel.code").value("C2"));
        mvc.perform(get(route(f.id(), "/next-question")).header("Authorization", "Bearer " + f.token())).andExpect(status().isNotFound());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/users/me/profile")
                .header("Authorization", "Bearer " + f.token()).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("fullName", "Học viên", "timezone", "UTC", "dailyLearningMinutes", 15))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.currentCefrLevel").doesNotExist());
        assertThat(jdbc.queryForObject("SELECT current_cefr_level_id FROM user_profiles WHERE user_id=?", UUID.class, f.owner().getId())).isNull();
    }

    @Test void reachesFiftyWithPartialBlockWithoutInventedLevelOrStability() throws Exception {
        Fixture f = fixture(AssessmentType.MANUAL);
        for (int n = 1; n <= 50; n++) {
            JsonNode body = accepted(f, n <= 48 && ((n - 1) / 4) % 2 == 0);
            assertThat(body.get("isCompleted").asBoolean()).isEqualTo(n == 50);
        }
        assertThat(score(f.id())).isEqualByComparingTo("48.00");
        assertThat(jdbc.queryForMap("SELECT block_questions,block_correct,stable_block_count FROM user_level_assessments WHERE id=?", f.id()))
                .containsEntry("block_questions", 2).containsEntry("block_correct", 0).containsEntry("stable_block_count", 0);
        assertThat(jdbc.queryForObject("SELECT final_cefr_level_id FROM user_level_assessments WHERE id=?", UUID.class, f.id())).isEqualTo(level("A1"));
        assertThat(count("assessment_items", "assessment_id", f.id())).isEqualTo(50);
        assertThat(jdbc.queryForObject("SELECT count(DISTINCT vocabulary_id) FROM assessment_items WHERE assessment_id=?", Integer.class, f.id())).isEqualTo(50);
    }

    @Test void scoreIsStoredEachAnswerAndHalfUpRatherThanTruncated() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        accepted(f, true); accepted(f, false); accepted(f, false);
        assertThat(score(f.id())).isEqualByComparingTo("33.33");
        Fixture rounded = fixture(AssessmentType.INITIAL);
        UUID q = question(rounded.id());
        jdbc.update("UPDATE user_level_assessments SET questions_answered=31,correct_answers=0,block_questions=3,block_correct=0 WHERE id=?", rounded.id());
        jdbc.update("UPDATE assessment_items SET sequence_no=32 WHERE id=?", q);
        submit(rounded.token(), rounded.id(), payload(q, correct(q), 0, UUID.randomUUID())).andExpect(status().isOk());
        assertThat(score(rounded.id())).isEqualByComparingTo("3.13");
    }

    @Test void replayPrecedesCatalogAndClosedGuardsAndReuseIsBoundToPathAndPayload() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        UUID q = question(f.id());
        UUID event = UUID.randomUUID();
        var body = payload(q, correct(q), 0, event);
        JsonNode response = mapper.readTree(submit(f.token(), f.id(), body).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        jdbc.update("UPDATE user_level_assessments SET status='CANCELLED' WHERE id=?", f.id());
        List<UUID> active = activeIds();
        jdbc.update("UPDATE vocabulary SET is_active=FALSE WHERE is_active=TRUE");
        try {
            assertThat(mapper.readTree(submit(f.token(), f.id(), body).andExpect(status().isOk()).andReturn().getResponse().getContentAsString())).isEqualTo(response);
            submit(f.token(), f.id(), payload(q, correct(q), 1, event)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
            submit(f.token(), UUID.randomUUID(), body).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
        } finally { restoreActive(active); }
    }

    @Test void unknownForeignClosedAndOutOfOrderQuestionGuardsDoNotMutate() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        Fixture other = fixture(AssessmentType.MANUAL);
        UUID q = question(f.id());
        submit(other.token(), f.id(), payload(q, correct(q), 0, UUID.randomUUID())).andExpect(status().isNotFound());
        submit(f.token(), UUID.randomUUID(), payload(q, correct(q), 0, UUID.randomUUID())).andExpect(status().isNotFound());
        submit(f.token(), f.id(), payload(question(other.id()), correct(q), 0, UUID.randomUUID())).andExpect(status().isNotFound());
        UUID future = UUID.randomUUID();
        jdbc.update("INSERT INTO assessment_items(id,assessment_id,sequence_no,vocabulary_id,cefr_level_id,question_text,options_json,correct_answer,created_at) SELECT ?,assessment_id,2,?,cefr_level_id,question_text,options_json,correct_answer,created_at FROM assessment_items WHERE id=?",
                future, unusedVocabulary(f.id()), q);
        String before = snapshot(f.id());
        submit(f.token(), f.id(), payload(future, correct(q), 0, UUID.randomUUID())).andExpect(status().isBadRequest());
        assertThat(snapshot(f.id())).isEqualTo(before);
        jdbc.update("UPDATE user_level_assessments SET status='CANCELLED' WHERE id=?", f.id());
        submit(f.token(), f.id(), payload(q, correct(q), 0, UUID.randomUUID())).andExpect(status().isBadRequest());
    }

    @Test void missingContentRollsBackCurrentAnswerStateAndClaimThenSameEventCanRetry() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        UUID q = question(f.id());
        UUID event = UUID.randomUUID();
        var body = payload(q, correct(q), 0, event);
        List<UUID> active = activeIds();
        String before = snapshot(f.id());
        jdbc.update("UPDATE vocabulary SET is_active=FALSE WHERE is_active=TRUE");
        try {
            submit(f.token(), f.id(), body).andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("ASSESSMENT_CONTENT_UNAVAILABLE"));
            assertThat(snapshot(f.id())).isEqualTo(before);
            assertThat(count("idempotency_keys", "event_id", event)).isZero();
        } finally { restoreActive(active); }
        submit(f.token(), f.id(), body).andExpect(status().isOk());
    }

    @Test void nextPersistenceFailureRollsBackAnswerAggregateAndClaim() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        UUID q = question(f.id());
        UUID event = UUID.randomUUID();
        String before = snapshot(f.id());
        jdbc.execute("CREATE FUNCTION reject_asmt004_item() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$");
        jdbc.execute("CREATE TRIGGER reject_asmt004_item BEFORE INSERT ON assessment_items FOR EACH ROW EXECUTE FUNCTION reject_asmt004_item()");
        try {
            submit(f.token(), f.id(), payload(q, correct(q), 0, event)).andExpect(status().isInternalServerError());
            assertThat(snapshot(f.id())).isEqualTo(before);
            assertThat(count("idempotency_keys", "event_id", event)).isZero();
        } finally {
            jdbc.execute("DROP TRIGGER reject_asmt004_item ON assessment_items");
            jdbc.execute("DROP FUNCTION reject_asmt004_item()");
        }
    }

    @Test void finalProfileFailureRollsBackTerminalAnswerAndResultAtomically() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        jdbc.update("INSERT INTO user_profiles(id,user_id,daily_learning_minutes,timezone,created_at,updated_at) VALUES (?,?,10,'UTC',?,?)", UUID.randomUUID(), f.owner().getId(), java.sql.Timestamp.from(NOW), java.sql.Timestamp.from(NOW));
        for (int n = 1; n < 20; n++) accepted(f, false);
        UUID q = question(f.id());
        UUID event = UUID.randomUUID();
        String wrong = wrong(q);
        String before = snapshot(f.id());
        jdbc.execute("CREATE FUNCTION reject_asmt004_profile() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$");
        jdbc.execute("CREATE TRIGGER reject_asmt004_profile BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION reject_asmt004_profile()");
        try {
            submit(f.token(), f.id(), payload(q, wrong, 0, event)).andExpect(status().isInternalServerError());
            assertThat(snapshot(f.id())).isEqualTo(before);
            assertThat(count("idempotency_keys", "event_id", event)).isZero();
        } finally {
            jdbc.execute("DROP TRIGGER reject_asmt004_profile ON user_profiles");
            jdbc.execute("DROP FUNCTION reject_asmt004_profile()");
        }
        submit(f.token(), f.id(), payload(q, wrong, 0, event)).andExpect(status().isOk()).andExpect(jsonPath("$.isCompleted").value(true));
    }

    @Test void terminalAnswerDoesNotRequireAvailableCatalog() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        for (int n = 1; n < 20; n++) accepted(f, false);
        UUID q = question(f.id());
        String wrong = wrong(q);
        List<UUID> active = activeIds();
        jdbc.update("UPDATE vocabulary SET is_active=FALSE WHERE is_active=TRUE");
        try {
            submit(f.token(), f.id(), payload(q, wrong, 0, UUID.randomUUID())).andExpect(status().isOk()).andExpect(jsonPath("$.isCompleted").value(true));
        } finally { restoreActive(active); }
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void concurrentSameOrDifferentEventsForOneQuestionCommitOneTransition(boolean sameEvent) throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        UUID q = question(f.id());
        String answer = correct(q);
        UUID shared = UUID.randomUUID();
        CyclicBarrier barrier = new CyclicBarrier(4);
        List<Callable<org.springframework.mock.web.MockHttpServletResponse>> tasks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            UUID event = sameEvent ? shared : UUID.randomUUID();
            tasks.add(() -> { barrier.await(10, TimeUnit.SECONDS); return submit(f.token(), f.id(), payload(q, answer, 1, event)).andReturn().getResponse(); });
        }
        try (var pool = Executors.newFixedThreadPool(4)) {
            List<org.springframework.mock.web.MockHttpServletResponse> results = new ArrayList<>();
            for (var future : pool.invokeAll(tasks)) results.add(future.get(30, TimeUnit.SECONDS));
            assertThat(results.stream().filter(r -> r.getStatus() == 200).count()).isEqualTo(sameEvent ? 4 : 1);
            JsonNode first = mapper.readTree(results.stream().filter(r -> r.getStatus() == 200).findFirst().orElseThrow().getContentAsString());
            for (var result : results) {
                JsonNode body = mapper.readTree(result.getContentAsString());
                if (result.getStatus() == 200) assertThat(body).isEqualTo(first);
                else { assertThat(result.getStatus()).isEqualTo(409); assertThat(body.get("code").asText()).isEqualTo("ASSESSMENT_QUESTION_ALREADY_ANSWERED"); }
            }
        }
        assertThat(jdbc.queryForObject("SELECT questions_answered FROM user_level_assessments WHERE id=?", Integer.class, f.id())).isEqualTo(1);
        assertThat(count("assessment_items", "assessment_id", f.id())).isEqualTo(2);
        assertThat(count("idempotency_keys", "user_id", f.owner().getId())).isEqualTo(2);
    }

    @ParameterizedTest @ValueSource(strings = {"{}", "null", "[]", "{\"questionId\":null}",
            "{\"questionId\":\"x\",\"answer\":\"a\",\"responseTimeMs\":0,\"eventId\":\"x\"}"})
    void malformedBodyIsRejected(String body) throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        mvc.perform(post(route(f.id(), "/answers")).header("Authorization", "Bearer " + f.token()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT questions_answered FROM user_level_assessments WHERE id=?", Integer.class, f.id())).isZero();
    }

    @Test void strictTypesUnknownFieldsRepeatedKeysTrailingJsonAndQueryAreRejected() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        UUID q = question(f.id());
        String valid = mapper.writeValueAsString(payload(q, correct(q), 0, UUID.randomUUID()));
        for (String bad : List.of(valid.replace("\"responseTimeMs\":0", "\"responseTimeMs\":-1"),
                valid.replace("\"responseTimeMs\":0", "\"responseTimeMs\":2147483648"),
                valid.replace("\"responseTimeMs\":0", "\"responseTimeMs\":0.0"),
                valid.replace("\"responseTimeMs\":0", "\"responseTimeMs\":\"0\""),
                valid.substring(0, valid.length() - 1) + ",\"extra\":true}",
                valid.substring(0, valid.length() - 1) + ",\"answer\":\"duplicate\"}", valid + " {}")) {
            mvc.perform(post(route(f.id(), "/answers")).header("Authorization", "Bearer " + f.token()).contentType("application/json").content(bad)).andExpect(status().isBadRequest());
        }
        mvc.perform(post(route(f.id(), "/answers") + "?x=1&x=2").header("Authorization", "Bearer " + f.token()).contentType("application/json").content(valid)).andExpect(status().isBadRequest());
        mvc.perform(post(AssessmentService.ROUTE + "/bad/answers").header("Authorization", "Bearer " + f.token()).contentType("application/json").content(valid)).andExpect(status().isBadRequest());
    }

    @Test void authenticationRoleMissingUserAndEndpointReuseAreEnforced() throws Exception {
        Fixture f = fixture(AssessmentType.INITIAL);
        UUID q = question(f.id());
        var body = payload(q, correct(q), 0, UUID.randomUUID());
        submit(null, f.id(), body).andExpect(status().isUnauthorized());
        submit(tokens.issue(f.owner().getId(), UserRole.ADMIN), f.id(), body).andExpect(status().isForbidden());
        submit(tokens.issue(UUID.randomUUID(), UserRole.USER), f.id(), body).andExpect(status().isUnauthorized());
        UUID startEvent = jdbc.queryForObject("SELECT event_id FROM idempotency_keys WHERE user_id=?", UUID.class, f.owner().getId());
        submit(f.token(), f.id(), payload(q, correct(q), 0, startEvent)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSE"));
    }

    private JsonNode accepted(Fixture f, boolean correct) throws Exception {
        UUID q = question(f.id());
        return mapper.readTree(submit(f.token(), f.id(), payload(q, correct ? correct(q) : wrong(q), 7, UUID.randomUUID()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private Fixture fixture(AssessmentType type) throws Exception {
        User owner = user();
        String token = tokens.issue(owner.getId(), UserRole.USER);
        String raw = mvc.perform(post(AssessmentService.ROUTE).header("Authorization", "Bearer " + token).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("type", type.name(), "eventId", UUID.randomUUID())))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new Fixture(owner, token, UUID.fromString(mapper.readTree(raw).get("assessmentId").asText()));
    }
    private User user() { return users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Học viên", passwords)); }
    private UUID question(UUID id) { return jdbc.queryForObject("SELECT id FROM assessment_items WHERE assessment_id=? AND answered_at IS NULL ORDER BY sequence_no LIMIT 1", UUID.class, id); }
    private String correct(UUID q) { return jdbc.queryForObject("SELECT correct_answer FROM assessment_items WHERE id=?", String.class, q); }
    private String wrong(UUID q) { return jdbc.queryForObject("SELECT value FROM assessment_items,jsonb_array_elements_text(options_json) WHERE id=? AND value<>correct_answer LIMIT 1", String.class, q); }
    private java.math.BigDecimal score(UUID id) { return jdbc.queryForObject("SELECT score FROM user_level_assessments WHERE id=?", java.math.BigDecimal.class, id); }
    private UUID level(String code) { return jdbc.queryForObject("SELECT id FROM cefr_levels WHERE code=?", UUID.class, code); }
    private UUID unusedVocabulary(UUID id) { return jdbc.queryForObject("SELECT id FROM vocabulary WHERE id NOT IN (SELECT vocabulary_id FROM assessment_items WHERE assessment_id=?) LIMIT 1", UUID.class, id); }
    private int count(String table, String field, UUID id) {
        // Tên bảng/cột chỉ là hằng số do fixtures kiểm thử cung cấp.
        return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE " + field + "=?", Integer.class, id);
    }
    private String snapshot(UUID id) { return jdbc.queryForObject("SELECT row_to_json(a)::text FROM user_level_assessments a WHERE id=?", String.class, id)
            + jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(i) ORDER BY sequence_no)::text FROM assessment_items i WHERE assessment_id=?", String.class, id); }
    private List<UUID> activeIds() { return jdbc.query("SELECT id FROM vocabulary WHERE is_active=TRUE", (rs,n) -> rs.getObject(1, UUID.class)); }
    private void restoreActive(List<UUID> active) { for (UUID id : active) jdbc.update("UPDATE vocabulary SET is_active=TRUE WHERE id=?", id); }
    private Map<String,Object> payload(UUID q, String answer, int time, UUID event) { return Map.of("questionId", q, "answer", answer, "responseTimeMs", time, "eventId", event); }
    private String route(UUID id, String suffix) { return AssessmentService.ROUTE + "/" + id + suffix; }
    private ResultActions submit(String token, UUID id, Map<String,?> payload) throws Exception {
        var request = post(route(id, "/answers")).contentType("application/json").content(mapper.writeValueAsString(payload));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return mvc.perform(request);
    }
    private static String secret() { byte[] value = new byte[32]; new SecureRandom().nextBytes(value); return Base64.getEncoder().encodeToString(value); }
    private record Fixture(User owner, String token, UUID id) { }
    @TestConfiguration static class TimeConfiguration { @Bean @Primary Clock clock() { return Clock.fixed(NOW, ZoneOffset.UTC); } }
}
