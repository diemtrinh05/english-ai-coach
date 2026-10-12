package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** QA-IDCAT-001: kiểm tra phiên do API cấp xuyên qua các module trên PostgreSQL thật. */
@AutoConfigureMockMvc
@Import(IdentityCatalogIntegrationTests.Configuration.class)
class IdentityCatalogIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired RefreshTokenHasher hasher;
    private final List<String> fixtureEmails = new ArrayList<>();
    private final List<UUID> fixtureGoals = new ArrayList<>();

    @DynamicPropertySource
    static void configuration(DynamicPropertyRegistry registry) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        registry.add("app.jwt.secret", () -> Base64.getEncoder().encodeToString(key));
        registry.add("app.jwt.refresh-token-expiration", () -> "7d");
        registry.add("app.google-identity.client-id", () -> GoogleTokenFixture.CLIENT);
    }

    @TestConfiguration
    static class Configuration {
        @Bean @Primary Clock time() { return Clock.fixed(GoogleTokenFixture.NOW, ZoneOffset.UTC); }
        @Bean @Primary GoogleJwksSource jwks(Clock clock) {
            return new GoogleJwksSource(clock, () -> new GoogleJwksSource.Document(
                    GoogleTokenFixture.jwks(GoogleTokenFixture.KEY), "max-age=3600"));
        }
    }

    // Không bọc test trong transaction: mỗi request phải commit thật trước request kế tiếp.
    @AfterEach
    void removeOnlyOwnedFixtures() {
        for (String email : fixtureEmails) {
            jdbc.update("DELETE FROM refresh_tokens WHERE user_id IN (SELECT id FROM users WHERE email=?)", email);
            jdbc.update("DELETE FROM user_goals WHERE user_id IN (SELECT id FROM users WHERE email=?)", email);
            jdbc.update("DELETE FROM user_profiles WHERE user_id IN (SELECT id FROM users WHERE email=?)", email);
            jdbc.update("DELETE FROM users WHERE email=?", email);
        }
        for (UUID id : fixtureGoals) jdbc.update("DELETE FROM goals WHERE id=?", id);
        for (String email : fixtureEmails)
            assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE email=?", Integer.class, email)).isZero();
    }

    @Test
    void localApiIssuedSessionsKeepProfileGoalsAndCatalogAcrossRotationAndLogout() throws Exception {
        Session registered = register();
        missingSetup(registered);
        saveSetup(registered, "  Tên giữ nguyên  ");
        Session login = session(json(call(post("/api/v1/auth/login"), null,
                Map.of("email", registered.email(), "password", "password")), 200), registered.email());
        assertThat(login.id()).isEqualTo(registered.id());
        assertThat(login.refresh()).isNotEqualTo(registered.refresh());
        ownSetup(login, "  Tên giữ nguyên  ");
        publicCatalog();

        Session rotated = rotate(login);
        assertThat(rotated.refresh()).isNotEqualTo(login.refresh());
        var oldRow = tokenRow(login.refresh());
        var newRow = tokenRow(rotated.refresh());
        assertThat(oldRow.get("revoked_at")).isNotNull();
        assertThat(newRow.get("revoked_at")).isNull();
        assertThat(newRow.get("expires_at")).isEqualTo(oldRow.get("expires_at"));
        assertThat(newRow.get("token_hash")).isNotEqualTo(rotated.refresh());
        rejectRefresh(login.refresh());
        ownSetup(rotated, "  Tên giữ nguyên  ");
        logout(rotated);
        Object revokedAt = tokenRow(rotated.refresh()).get("revoked_at");
        logout(rotated);
        assertThat(tokenRow(rotated.refresh()).get("revoked_at")).isEqualTo(revokedAt);
        rejectRefresh(rotated.refresh());
        ownSetup(rotated, "  Tên giữ nguyên  "); // Access JWT không bị blacklist khi logout.
        assertThat(tokenRow(registered.refresh()).get("revoked_at")).isNull();
        ownSetup(rotate(registered), "  Tên giữ nguyên  ");
        publicCatalog();
    }

    @Test
    void signedGoogleIdentityUsesSameProtectedSetupAndSessionLifecycleWithoutAutoLink() throws Exception {
        String email = email();
        String subject = UUID.randomUUID().toString();
        Session google = session(json(call(post("/api/v1/auth/google"), null,
                Map.of("idToken", GoogleTokenFixture.token(subject, email, "Google"))), 200), email);
        missingSetup(google);
        saveSetup(google, "Hồ sơ Google");
        Session returning = session(json(call(post("/api/v1/auth/google"), null,
                Map.of("idToken", GoogleTokenFixture.token(subject, email(), null))), 200), email);
        assertThat(returning.id()).isEqualTo(google.id());
        ownSetup(returning, "Hồ sơ Google");
        error(call(post("/api/v1/auth/login"), null, Map.of("email", email, "password", "password")),
                401, "AUTH_INVALID_CREDENTIALS");
        error(call(post("/api/v1/auth/google"), null,
                Map.of("idToken", GoogleTokenFixture.token(UUID.randomUUID().toString(), email, "Khác"))),
                409, "CONFLICT");
        error(call(post("/api/v1/auth/google"), null, Map.of("idToken", "invalid")), 401, "UNAUTHORIZED");
        assertThat(jdbc.queryForMap("SELECT auth_provider,password_hash FROM users WHERE id=?", google.id()))
                .containsEntry("auth_provider", "GOOGLE").containsEntry("password_hash", null);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE user_id=?", Integer.class, google.id()))
                .isEqualTo(2);
        Session rotated = rotate(returning);
        ownSetup(rotated, "Hồ sơ Google");
        logout(rotated);
        rejectRefresh(rotated.refresh());
        assertThat(tokenRow(google.refresh()).get("revoked_at")).isNull();
        publicCatalog();
    }

    @Test
    void twoApiUsersCannotTargetForeignProfileGoalsOrRefreshSession() throws Exception {
        Session owner = register();
        Session caller = register();
        saveSetup(owner, "Hồ sơ riêng owner");
        var request = profile("Hồ sơ riêng caller");
        request.put("userId", owner.id().toString());
        request.put("role", "ADMIN");
        request.put("currentCefrLevelId", UUID.randomUUID().toString());
        json(call(put("/api/v1/users/me/profile"), caller, request), 200);
        UUID goal = goalIds().get(1);
        json(call(put("/api/v1/users/me/goals"), caller,
                Map.of("goalIds", List.of(goal), "userId", owner.id().toString())), 200);
        ownSetup(owner, "Hồ sơ riêng owner");
        assertThat(json(call(get("/api/v1/users/me"), caller, null), 200).get("id").asString())
                .isEqualTo(caller.id().toString());
        assertThat(json(call(get("/api/v1/users/me"), caller, null), 200).get("role").asString()).isEqualTo("USER");
        assertThat(json(call(get("/api/v1/users/me/profile"), caller, null), 200).get("currentCefrLevel")).isNull();
        assertThat(json(call(get("/api/v1/users/me/goals"), caller, null), 200).get("goals").get(0).get("id").asString())
                .isEqualTo(goal.toString());
        error(call(post("/api/v1/auth/logout"), caller, Map.of("refreshToken", owner.refresh())),
                401, "AUTH_REFRESH_TOKEN_INVALID");
        assertThat(tokenRow(owner.refresh()).get("revoked_at")).isNull();
        assertThat(tokenRow(caller.refresh()).get("revoked_at")).isNull();
        logout(caller);
        ownSetup(rotate(owner), "Hồ sơ riêng owner");
    }

    @Test
    void invalidAuthAndSetupRequestsPreserveCommittedStateAndAllowExplicitCorrection() throws Exception {
        Session user = register();
        saveSetup(user, "Tên ban đầu");
        int tokensBefore = jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE user_id=?", Integer.class, user.id());
        error(call(post("/api/v1/auth/register"), null,
                Map.of("email", user.email(), "password", "password", "fullName", "Đổi")), 409, "CONFLICT");
        error(call(post("/api/v1/auth/login"), null,
                Map.of("email", user.email(), "password", "wrong-password")), 401, "AUTH_INVALID_CREDENTIALS");
        error(call(post("/api/v1/auth/refresh"), null, Map.of()), 400, "VALIDATION_ERROR");
        var badProfile = profile("Không được lưu");
        badProfile.put("timezone", "+07:00");
        error(call(put("/api/v1/users/me/profile"), user, badProfile), 400, "VALIDATION_ERROR");
        ownSetup(user, "Tên ban đầu");
        UUID selected = goalIds().get(0);
        error(call(put("/api/v1/users/me/goals"), user,
                Map.of("goalIds", List.of(selected, selected))), 400, "VALIDATION_ERROR");
        error(call(put("/api/v1/users/me/goals"), user,
                Map.of("goalIds", List.of(UUID.randomUUID()))), 404, "NOT_FOUND");
        error(call(post("/api/v1/auth/logout"), user, Map.of()), 400, "VALIDATION_ERROR");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE user_id=?", Integer.class, user.id()))
                .isEqualTo(tokensBefore);
        assertThat(tokenRow(user.refresh()).get("revoked_at")).isNull();
        ownSetup(user, "Tên ban đầu");
        ownSetup(rotate(user), "Tên ban đầu");
    }

    @Test
    void profileCommitSurvivesRejectedGoalSaveAndRetryChangesOnlyGoals() throws Exception {
        Session user = register();
        saveSetup(user, "Ban đầu");
        UUID inactive = UUID.randomUUID();
        fixtureGoals.add(inactive);
        jdbc.update("INSERT INTO goals(id,name,is_active,created_at) VALUES(?,?,FALSE,CURRENT_TIMESTAMP)",
                inactive, "QA-IDCAT-" + inactive);
        json(call(put("/api/v1/users/me/profile"), user, profile("Profile đã lưu")), 200);
        error(call(put("/api/v1/users/me/goals"), user, Map.of("goalIds", List.of(inactive))), 400, "VALIDATION_ERROR");
        ownSetup(user, "Profile đã lưu");
        // Timestamp quá khứ giúp phát hiện rewrite hồ sơ dù Clock test cố định.
        jdbc.update("UPDATE user_profiles SET created_at=?,updated_at=? WHERE user_id=?",
                java.sql.Timestamp.from(GoogleTokenFixture.NOW.minusSeconds(1000)),
                java.sql.Timestamp.from(GoogleTokenFixture.NOW.minusSeconds(500)), user.id());
        Object updatedAt = jdbc.queryForObject("SELECT updated_at FROM user_profiles WHERE user_id=?", Object.class, user.id());
        UUID replacement = goalIds().get(1);
        JsonNode response = json(call(put("/api/v1/users/me/goals"), user,
                Map.of("goalIds", List.of(replacement))), 200);
        assertThat(response.get("goals").get(0).get("id").asString()).isEqualTo(replacement.toString());
        assertThat(response.get("goals").get(0).get("isPrimary").asBoolean()).isFalse();
        assertThat(jdbc.queryForObject("SELECT updated_at FROM user_profiles WHERE user_id=?", Object.class, user.id()))
                .isEqualTo(updatedAt);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_goals WHERE user_id=?", Integer.class, user.id())).isEqualTo(1);
    }

    @Test
    void protectedIdentityBoundaryAndPublicCatalogErrorsRemainDistinctForApiIssuedUser() throws Exception {
        Session user = register();
        for (String path : List.of("/api/v1/users/me", "/api/v1/users/me/profile", "/api/v1/users/me/goals")) {
            error(call(get(path), null, null), 401, "UNAUTHORIZED");
            error(mvc.perform(get(path).header("Authorization", "Bearer invalid")), 401, "UNAUTHORIZED");
        }
        error(call(put("/api/v1/users/me/profile"), null, profile("Không lưu")), 401, "UNAUTHORIZED");
        error(call(put("/api/v1/users/me/goals"), null, Map.of("goalIds", goalIds())), 401, "UNAUTHORIZED");
        error(call(post("/api/v1/auth/logout"), null, Map.of("refreshToken", user.refresh())), 401, "UNAUTHORIZED");
        error(call(get("/api/v1/admin/users"), user, null), 403, "FORBIDDEN");
        error(call(get("/api/v1/topics").param("size", "101"), null, null), 400, "VALIDATION_ERROR");
        error(call(get("/api/v1/vocabulary").param("cefr", "X9"), null, null), 400, "VALIDATION_ERROR");
        error(call(get("/api/v1/vocabulary/" + UUID.randomUUID()), null, null), 404, "NOT_FOUND");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_profiles WHERE user_id=?", Integer.class, user.id())).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_goals WHERE user_id=?", Integer.class, user.id())).isZero();
        assertThat(tokenRow(user.refresh()).get("revoked_at")).isNull();
        publicCatalog();
    }

    private String email() { String email = UUID.randomUUID() + "@example.test"; fixtureEmails.add(email); return email; }
    private Session register() throws Exception {
        String email = email();
        return session(json(call(post("/api/v1/auth/register"), null,
                Map.of("email", email, "password", "password", "fullName", "Người học")), 201), email);
    }
    private Session session(JsonNode data, String email) {
        return new Session(UUID.fromString(data.get("user").get("id").asString()), email,
                data.get("accessToken").asString(), data.get("refreshToken").asString());
    }
    private ResultActions call(MockHttpServletRequestBuilder request, Session user, Object body) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.access());
        if (body != null) request.contentType("application/json").content(mapper.writeValueAsString(body));
        return mvc.perform(request);
    }
    private JsonNode json(ResultActions result, int status) throws Exception {
        return mapper.readTree(result.andExpect(status().is(status)).andReturn().getResponse().getContentAsString());
    }
    private void error(ResultActions result, int expected, String code) throws Exception {
        result.andExpect(status().is(expected)).andExpect(jsonPath("$.code").value(code));
    }
    private Map<String, Object> profile(String name) {
        return new LinkedHashMap<>(Map.of("fullName", name, "timezone", "Asia/Ho_Chi_Minh", "dailyLearningMinutes", 15));
    }
    private List<UUID> goalIds() throws Exception {
        JsonNode goals = json(call(get("/api/v1/goals"), null, null), 200);
        return List.of(UUID.fromString(goals.get(0).get("id").asString()), UUID.fromString(goals.get(1).get("id").asString()));
    }
    private void missingSetup(Session user) throws Exception {
        error(call(get("/api/v1/users/me/profile"), user, null), 404, "NOT_FOUND");
        assertThat(json(call(get("/api/v1/users/me/goals"), user, null), 200).get("goals").size()).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_profiles WHERE user_id=?", Integer.class, user.id())).isZero();
    }
    private void saveSetup(Session user, String name) throws Exception {
        json(call(put("/api/v1/users/me/profile"), user, profile(name)), 200);
        UUID goal = goalIds().get(0);
        json(call(put("/api/v1/users/me/goals"), user, Map.of("goalIds", List.of(goal), "primaryGoalId", goal)), 200);
        ownSetup(user, name);
    }
    private void ownSetup(Session user, String name) throws Exception {
        JsonNode summary = json(call(get("/api/v1/users/me"), user, null), 200);
        assertThat(summary.get("id").asString()).isEqualTo(user.id().toString());
        assertThat(summary.get("fullName").asString()).isEqualTo(name);
        JsonNode profile = json(call(get("/api/v1/users/me/profile"), user, null), 200);
        assertThat(profile.get("fullName").asString()).isEqualTo(name);
        assertThat(profile.get("timezone").asString()).isEqualTo("Asia/Ho_Chi_Minh");
        assertThat(profile.get("dailyLearningMinutes").asInt()).isEqualTo(15);
        assertThat(profile.get("currentCefrLevel")).isNull();
        JsonNode goals = json(call(get("/api/v1/users/me/goals"), user, null), 200);
        assertThat(goals.get("goals").get(0).get("id").asString()).isEqualTo(goalIds().get(0).toString());
        assertThat(goals.get("goals").get(0).get("isPrimary").asBoolean()).isTrue();
        for (JsonNode response : List.of(summary, profile, goals)) {
            assertThat(response.toString()).doesNotContain(user.access(), user.refresh(), "passwordHash", "tokenHash", "refreshToken");
        }
    }
    private Session rotate(Session user) throws Exception {
        JsonNode pair = json(call(post("/api/v1/auth/refresh"), null, Map.of("refreshToken", user.refresh())), 200);
        return new Session(user.id(), user.email(), pair.get("accessToken").asString(), pair.get("refreshToken").asString());
    }
    private void logout(Session user) throws Exception {
        var response = call(post("/api/v1/auth/logout"), user, Map.of("refreshToken", user.refresh()))
                .andExpect(status().isNoContent()).andReturn().getResponse();
        assertThat(response.getContentAsString()).isEmpty();
    }
    private void rejectRefresh(String token) throws Exception {
        error(call(post("/api/v1/auth/refresh"), null, Map.of("refreshToken", token)), 401, "AUTH_REFRESH_TOKEN_INVALID");
    }
    private Map<String, Object> tokenRow(String raw) {
        return jdbc.queryForMap("SELECT token_hash,expires_at,revoked_at FROM refresh_tokens WHERE token_hash=?", hasher.hash(raw));
    }
    private void publicCatalog() throws Exception {
        JsonNode levels = json(call(get("/api/v1/cefr-levels"), null, null), 200);
        assertThat(levels.size()).isEqualTo(6);
        for (JsonNode level : levels) {
            JsonNode words = json(call(get("/api/v1/vocabulary").param("cefr", level.get("code").asString()).param("size", "1"), null, null), 200);
            assertThat(words.get("totalElements").asInt()).isGreaterThanOrEqualTo(30);
            String id = words.get("content").get(0).get("id").asString();
            JsonNode detail = json(call(get("/api/v1/vocabulary/" + id), null, null), 200);
            assertThat(detail.get("meaningVi").asString()).isNotBlank();
            assertThat(detail.get("examples").isArray()).isTrue();
            assertThat(json(call(get("/api/v1/vocabulary/" + id + "/examples"), null, null), 200).isArray()).isTrue();
            assertThat(detail.toString()).doesNotContain("refreshToken", "passwordHash");
        }
        JsonNode topics = json(call(get("/api/v1/topics").param("size", "1"), null, null), 200);
        String topic = topics.get("content").get(0).get("id").asString();
        assertThat(json(call(get("/api/v1/topics/" + topic), null, null), 200).get("isActive").asBoolean()).isTrue();
        assertThat(json(call(get("/api/v1/goals"), null, null), 200).size()).isGreaterThanOrEqualTo(2);
    }
    private record Session(UUID id, String email, String access, String refresh) {
        @Override public String toString() { return "Session[redacted]"; }
    }
}
