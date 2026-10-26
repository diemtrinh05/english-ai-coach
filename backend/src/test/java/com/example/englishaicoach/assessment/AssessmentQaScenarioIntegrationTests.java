package com.example.englishaicoach.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.auth.JwtAccessTokenService;
import com.example.englishaicoach.auth.PasswordHashService;
import com.example.englishaicoach.auth.User;
import com.example.englishaicoach.auth.UserRepository;
import com.example.englishaicoach.auth.UserRole;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Đường đi golden qua HTTP boundary và SQL committed; không dùng calculator làm oracle. */
@AutoConfigureMockMvc
@Import(AssessmentQaScenarioIntegrationTests.FixedTime.class)
class AssessmentQaScenarioIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-26T08:00:00Z");
    private static final String SECRET = secret();
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService tokens;

    @DynamicPropertySource static void settings(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> SECRET);
    }

    @Test
    void mixedPromotionDemotionAndHoldsMatchLiteralCommittedTrajectory() throws Exception {
        Fixture f = start();
        // Chỉ dùng bảng literal đã chốt; HOLD trước lần promote không được mang sang level mới.
        String[] patterns = {"CTCC", "CCCT", "TTCT", "CTCT", "CCTC", "TCCT", "CCTT"};
        String[] levels = {"A2", "B1", "A2", "A2", "B1", "B1", "B1"};
        int[] stable = {0, 0, 0, 1, 0, 1, 2};
        int[] correct = {3, 6, 7, 9, 12, 14, 16};
        int sequence = 0;
        String previousLevel = "A1";
        JsonNode finalResponse = null;
        for (int block = 0; block < patterns.length; block++) {
            for (int position = 0; position < 4; position++) {
                sequence++;
                finalResponse = accepted(f, patterns[block].charAt(position) == 'C');
                assertThat(finalResponse.get("questionsAnswered").asInt()).isEqualTo(sequence);
                assertThat(finalResponse.get("isCompleted").asBoolean()).isEqualTo(sequence == 28);
                assertThat(finalResponse.get("nextDifficulty").asText())
                        .isEqualTo(position == 3 ? levels[block] : previousLevel);
                if (sequence < 28) {
                    assertThat(jdbc.queryForObject("SELECT sequence_no FROM assessment_items WHERE assessment_id=? AND answered_at IS NULL",
                            Integer.class, f.id())).isEqualTo(sequence + 1);
                }
            }
            assertBlock(f, sequence, correct[block], stable[block], levels[block]);
            previousLevel = levels[block];
        }
        assertTerminal(f, "B1", 28, "57.14");
        assertThat(finalResponse.get("correct").asBoolean()).isFalse();
    }

    @Test
    void maximumFiftyKeepsC1DespiteTwoWrongAnswersInThePartialBlock() throws Exception {
        Fixture f = start();
        String[] patterns = {"CCCT", "CCTC", "TCCC", "CTCC", "CCCT", "TTCT",
                "CTCC", "CTTT", "CCTC", "TTTC", "TCCC", "TCTT"};
        String[] levels = {"A2", "B1", "B2", "C1", "C2", "C1", "C2", "C1", "C2", "C1", "C2", "C1"};
        int[] cumulative = {3, 6, 9, 12, 15, 16, 19, 20, 23, 24, 27, 28};
        int total = 0;
        for (int block = 0; block < patterns.length; block++) {
            for (char answer : patterns[block].toCharArray()) {
                total++;
                assertThat(accepted(f, answer == 'C').get("isCompleted").asBoolean()).isFalse();
            }
            assertBlock(f, total, cumulative[block], 0, levels[block]);
        }
        JsonNode fortyNine = accepted(f, false);
        assertThat(fortyNine.get("nextDifficulty").asText()).isEqualTo("C1");
        assertThat(fortyNine.get("isCompleted").asBoolean()).isFalse();
        JsonNode fifty = accepted(f, false);
        assertThat(fifty.get("nextDifficulty").asText()).isEqualTo("C1");
        assertThat(fifty.get("isCompleted").asBoolean()).isTrue();
        assertThat(jdbc.queryForMap("SELECT block_questions,block_correct,stable_block_count FROM user_level_assessments WHERE id=?", f.id()))
                .containsEntry("block_questions", 2).containsEntry("block_correct", 0).containsEntry("stable_block_count", 0);
        assertTerminal(f, "C1", 50, "56.00");
    }

    @ParameterizedTest
    @EnumSource(CefrDifficulty.class)
    void startRejectsTwentyNineUsableRowsAtEveryLevelAndSameEventRecovers(CefrDifficulty level) throws Exception {
        User owner = user();
        String token = tokens.issue(owner.getId(), UserRole.USER);
        UUID event = UUID.randomUUID();
        List<UUID> active = active(level);
        assertThat(active.size()).isGreaterThanOrEqualTo(30);
        jdbc.update("UPDATE vocabulary SET is_active=FALSE WHERE cefr_level_id=(SELECT id FROM cefr_levels WHERE code=?)", level.name());
        for (UUID id : active.subList(0, 29)) jdbc.update("UPDATE vocabulary SET is_active=TRUE WHERE id=?", id);
        try {
            var result = startRequest(token, event);
            assertThat(result.getStatus()).isEqualTo(503);
            assertThat(json(result).get("code").asText()).isEqualTo("ASSESSMENT_CONTENT_UNAVAILABLE");
            assertThat(count("SELECT count(*) FROM idempotency_keys WHERE event_id=?", event)).isZero();
            assertThat(count("SELECT count(*) FROM user_level_assessments WHERE user_id=?", owner.getId())).isZero();
        } finally { restore(active); }
        MockHttpServletResponse first = startRequest(token, event);
        assertThat(first.getStatus()).isEqualTo(201);
        assertThat(json(startRequest(token, event))).isEqualTo(json(first));
        assertThat(count("SELECT count(*) FROM user_level_assessments WHERE user_id=?", owner.getId())).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"word", "part_of_speech", "meaning_vi"})
    void thirtyActiveRowsAreInsufficientWhenOneOffTargetRowIsBlank(String field) throws Exception {
        List<UUID> rows = active(CefrDifficulty.C2);
        assertThat(rows).hasSize(30);
        UUID id = rows.getFirst();
        // Tên cột chỉ lấy từ literal ValueSource của test, không nhận từ input HTTP.
        String original = jdbc.queryForObject("SELECT " + field + " FROM vocabulary WHERE id=?", String.class, id);
        User owner = user();
        UUID event = UUID.randomUUID();
        try {
            jdbc.update("UPDATE vocabulary SET " + field + "=? WHERE id=?", " \t", id);
            MockHttpServletResponse response = startRequest(tokens.issue(owner.getId(), UserRole.USER), event);
            assertThat(response.getStatus()).isEqualTo(503);
            assertThat(json(response).get("code").asText()).isEqualTo("ASSESSMENT_CONTENT_UNAVAILABLE");
            assertThat(count("SELECT count(*) FROM idempotency_keys WHERE event_id=?", event)).isZero();
        } finally { jdbc.update("UPDATE vocabulary SET " + field + "=? WHERE id=?", original, id); }
    }

    @Test
    void unavailableNextLevelRollsBackTheFourthAnswerAndRetryPromotesExactlyOnce() throws Exception {
        Fixture f = start();
        accepted(f, true); accepted(f, true); accepted(f, true);
        UUID q = question(f.id());
        Map<String, Object> payload = payload(q, false, UUID.randomUUID());
        UUID event = (UUID) payload.get("eventId");
        String before = snapshot(f.id());
        List<UUID> active = active(CefrDifficulty.A2);
        UUID disabled = active.getFirst();
        jdbc.update("UPDATE vocabulary SET is_active=FALSE WHERE id=?", disabled);
        try {
            MockHttpServletResponse failure = submit(f, payload);
            assertThat(failure.getStatus()).isEqualTo(503);
            assertThat(json(failure).get("code").asText()).isEqualTo("ASSESSMENT_CONTENT_UNAVAILABLE");
            assertThat(snapshot(f.id())).isEqualTo(before);
            assertThat(count("SELECT count(*) FROM idempotency_keys WHERE event_id=?", event)).isZero();
        } finally { jdbc.update("UPDATE vocabulary SET is_active=TRUE WHERE id=?", disabled); }
        MockHttpServletResponse result = submit(f, payload);
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(json(result).get("nextDifficulty").asText()).isEqualTo("A2");
        assertThat(json(result).get("questionsAnswered").asInt()).isEqualTo(4);
        String after = snapshot(f.id());
        assertThat(json(submit(f, payload))).isEqualTo(json(result));
        assertThat(snapshot(f.id())).isEqualTo(after);
        assertBlock(f, 4, 3, 0, "A2");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void concurrentTerminalAnswersProduceOneCompletionAndNoExtraQuestion(boolean sameEvent) throws Exception {
        Fixture f = start();
        jdbc.update("INSERT INTO user_profiles(id,user_id,current_cefr_level_id,daily_learning_minutes,timezone,created_at,updated_at) "
                + "VALUES (?,?,(SELECT id FROM cefr_levels WHERE code='C2'),35,'Asia/Ho_Chi_Minh',?,?)",
                UUID.randomUUID(), f.owner(), Timestamp.from(NOW.minusSeconds(60)), Timestamp.from(NOW.minusSeconds(60)));
        for (int i = 0; i < 19; i++) accepted(f, false);
        UUID question = question(f.id());
        UUID shared = UUID.randomUUID();
        CyclicBarrier barrier = new CyclicBarrier(4);
        List<UUID> events = new ArrayList<>();
        List<Callable<MockHttpServletResponse>> tasks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            UUID event = sameEvent ? shared : UUID.randomUUID();
            events.add(event);
            Map<String, Object> body = payload(question, false, event);
            tasks.add(() -> { barrier.await(10, TimeUnit.SECONDS); return submit(f, body); });
        }
        List<MockHttpServletResponse> responses = new ArrayList<>();
        try (var pool = Executors.newFixedThreadPool(4)) {
            for (var future : pool.invokeAll(tasks)) responses.add(future.get(30, TimeUnit.SECONDS));
        }
        assertThat(responses.stream().filter(r -> r.getStatus() == 200).count()).isEqualTo(sameEvent ? 4 : 1);
        JsonNode winning = json(responses.stream().filter(r -> r.getStatus() == 200).findFirst().orElseThrow());
        for (int i = 0; i < responses.size(); i++) {
            MockHttpServletResponse response = responses.get(i);
            if (response.getStatus() == 200) {
                assertThat(json(response)).isEqualTo(winning);
                assertThat(count("SELECT count(*) FROM idempotency_keys WHERE event_id=?", events.get(i))).isEqualTo(1);
            } else {
                assertThat(response.getStatus()).isEqualTo(409);
                assertThat(json(response).get("code").asText()).isEqualTo("ASSESSMENT_QUESTION_ALREADY_ANSWERED");
                assertThat(count("SELECT count(*) FROM idempotency_keys WHERE event_id=?", events.get(i))).isZero();
            }
        }
        assertThat(winning.get("questionsAnswered").asInt()).isEqualTo(20);
        assertThat(winning.get("isCompleted").asBoolean()).isTrue();
        assertTerminal(f, "A1", 20, "0.00");
        assertThat(jdbc.queryForMap("SELECT c.code,p.daily_learning_minutes,p.timezone FROM user_profiles p "
                + "JOIN cefr_levels c ON c.id=p.current_cefr_level_id WHERE user_id=?", f.owner()))
                .containsEntry("code", "A1").containsEntry("daily_learning_minutes", 35).containsEntry("timezone", "Asia/Ho_Chi_Minh");
        assertThat(count("SELECT count(*) FROM idempotency_keys WHERE user_id=?", f.owner())).isEqualTo(21);
    }

    private void assertBlock(Fixture f, int answered, int correct, int stable, String level) {
        assertThat(jdbc.queryForMap("SELECT a.questions_answered,a.correct_answers,a.block_questions,a.block_correct,a.stable_block_count,c.code "
                + "FROM user_level_assessments a JOIN cefr_levels c ON c.id=a.current_cefr_level_id WHERE a.id=?", f.id()))
                .containsEntry("questions_answered", answered).containsEntry("correct_answers", correct)
                .containsEntry("block_questions", 0).containsEntry("block_correct", 0)
                .containsEntry("stable_block_count", stable).containsEntry("code", level);
    }

    private void assertTerminal(Fixture f, String level, int questions, String score) throws Exception {
        assertThat(jdbc.queryForMap("SELECT a.status,c.code FROM user_level_assessments a "
                + "JOIN cefr_levels c ON c.id=a.final_cefr_level_id WHERE a.id=?", f.id()))
                .containsEntry("status", "COMPLETED").containsEntry("code", level);
        assertThat(jdbc.queryForObject("SELECT score FROM user_level_assessments WHERE id=?", java.math.BigDecimal.class, f.id()))
                .isEqualByComparingTo(score);
        assertThat(count("SELECT count(*) FROM assessment_items WHERE assessment_id=?", f.id())).isEqualTo(questions);
        assertThat(count("SELECT count(DISTINCT vocabulary_id) FROM assessment_items WHERE assessment_id=?", f.id())).isEqualTo(questions);
        assertThat(count("SELECT count(*) FROM assessment_items WHERE assessment_id=? AND answered_at IS NULL", f.id())).isZero();
        for (String table : List.of("xp_logs", "streaks", "user_vocabulary_progress")) {
            assertThat(count("SELECT count(*) FROM " + table + " WHERE user_id=?", f.owner())).isZero();
        }
        var response = mvc.perform(get(route(f.id(), "")).header("Authorization", "Bearer " + f.token()))
                .andExpect(status().isOk()).andReturn().getResponse();
        assertThat(json(response).get("cefrLevel").get("code").asText()).isEqualTo(level);
        assertThat(json(response).get("totalQuestions").asInt()).isEqualTo(questions);
    }

    private Fixture start() throws Exception {
        User owner = user();
        String token = tokens.issue(owner.getId(), UserRole.USER);
        MockHttpServletResponse response = startRequest(token, UUID.randomUUID());
        assertThat(response.getStatus()).isEqualTo(201);
        return new Fixture(owner.getId(), token, UUID.fromString(json(response).get("assessmentId").asText()));
    }
    private User user() { return users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Học viên", passwords)); }
    private MockHttpServletResponse startRequest(String token, UUID event) throws Exception {
        return mvc.perform(post("/api/v1/assessments").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(mapper.writeValueAsString(Map.of("type", "INITIAL", "eventId", event))))
                .andReturn().getResponse();
    }
    private JsonNode accepted(Fixture f, boolean correct) throws Exception {
        MockHttpServletResponse response = submit(f, payload(question(f.id()), correct, UUID.randomUUID()));
        assertThat(response.getStatus()).isEqualTo(200);
        return json(response);
    }
    private Map<String, Object> payload(UUID question, boolean correct, UUID event) {
        String answer = correct
                ? jdbc.queryForObject("SELECT correct_answer FROM assessment_items WHERE id=?", String.class, question)
                : jdbc.queryForObject("SELECT value FROM assessment_items,jsonb_array_elements_text(options_json) WHERE id=? AND value<>correct_answer LIMIT 1", String.class, question);
        return Map.of("questionId", question, "answer", answer, "responseTimeMs", 17, "eventId", event);
    }
    private MockHttpServletResponse submit(Fixture f, Map<String, Object> payload) throws Exception {
        return mvc.perform(post(route(f.id(), "/answers")).header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(mapper.writeValueAsString(payload))).andReturn().getResponse();
    }
    private UUID question(UUID id) { return jdbc.queryForObject("SELECT id FROM assessment_items WHERE assessment_id=? AND answered_at IS NULL ORDER BY sequence_no LIMIT 1", UUID.class, id); }
    private List<UUID> active(CefrDifficulty level) { return jdbc.query("SELECT v.id FROM vocabulary v JOIN cefr_levels c ON c.id=v.cefr_level_id WHERE c.code=? AND v.is_active ORDER BY v.id", (rs, n) -> rs.getObject(1, UUID.class), level.name()); }
    private void restore(List<UUID> ids) { for (UUID id : ids) jdbc.update("UPDATE vocabulary SET is_active=TRUE WHERE id=?", id); }
    private int count(String sql, UUID id) { return jdbc.queryForObject(sql, Integer.class, id); }
    private String snapshot(UUID id) { return jdbc.queryForObject("SELECT row_to_json(a)::text FROM user_level_assessments a WHERE id=?", String.class, id)
            + jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(i) ORDER BY sequence_no)::text FROM assessment_items i WHERE assessment_id=?", String.class, id); }
    private String route(UUID id, String suffix) { return "/api/v1/assessments/" + id + suffix; }
    private JsonNode json(MockHttpServletResponse response) throws Exception { return mapper.readTree(response.getContentAsString(StandardCharsets.UTF_8)); }
    private static String secret() { byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes); return Base64.getEncoder().encodeToString(bytes); }
    private record Fixture(UUID owner, String token, UUID id) { }
    @TestConfiguration static class FixedTime { @Bean @Primary Clock clock() { return Clock.fixed(NOW, ZoneOffset.UTC); } }
}
