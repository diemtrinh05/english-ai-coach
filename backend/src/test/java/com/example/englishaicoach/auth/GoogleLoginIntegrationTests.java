package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
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
@Import(GoogleLoginIntegrationTests.Configuration.class)
class GoogleLoginIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired RefreshTokenRepository tokens;
    @Autowired RefreshTokenHasher hasher;
    @Autowired JwtAccessTokenService jwt;
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        byte[] key=new byte[32]; new java.security.SecureRandom().nextBytes(key);
        registry.add("app.jwt.secret",()->Base64.getEncoder().encodeToString(key));
        registry.add("app.jwt.refresh-token-expiration",()->"7d");
        registry.add("app.google-identity.client-id",()->GoogleTokenFixture.CLIENT);
    }
    @TestConfiguration static class Configuration {
        @Bean @Primary Clock time() { return Clock.fixed(GoogleTokenFixture.NOW,ZoneOffset.UTC); }
        @Bean @Primary GoogleJwksSource fakeJwks(Clock clock) {
            return new GoogleJwksSource(clock,()->new GoogleJwksSource.Document(GoogleTokenFixture.jwks(GoogleTokenFixture.KEY),"max-age=3600"));
        }
    }
    String email() { return UUID.randomUUID()+"@example.test"; }
    String body(String sub,String email,String name) throws Exception {
        return mapper.writeValueAsString(Map.of("idToken",GoogleTokenFixture.token(sub,email,name)));
    }
    @Test void createsVerifiedGoogleUserWithAtomicHashedSessionAndNoProfile() throws Exception {
        String sub=UUID.randomUUID().toString(), email=email();
        var result=mvc.perform(post("/api/v1/auth/google").contentType("application/json")
                .content(body(sub,email,"Người học").replace("}",",\"role\":\"ADMIN\",\"email\":\"attacker@example.test\"}")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.role").value("USER")).andExpect(jsonPath("$.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.expiresIn").value(900)).andExpect(jsonPath("$.tokenType").value("Bearer")).andReturn();
        var response=mapper.readTree(result.getResponse().getContentAsString());
        UUID id=UUID.fromString(response.get("user").get("id").asString());
        assertThat(jwt.verify(response.get("accessToken").asString()).userId()).isEqualTo(id);
        var token=tokens.findByTokenHash(hasher.hash(response.get("refreshToken").asString())).orElseThrow();
        assertThat(token.getUserId()).isEqualTo(id); assertThat(token.getExpiresAt()).isEqualTo(GoogleTokenFixture.NOW.plusSeconds(7*86400));
        assertThat(jdbc.queryForMap("SELECT password_hash,auth_provider,provider_user_id FROM users WHERE id=?",id))
                .containsEntry("password_hash",null).containsEntry("auth_provider","GOOGLE").containsEntry("provider_user_id",sub);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_profiles WHERE user_id=?",Integer.class,id)).isZero();
    }
    @Test void returningSubjectKeepsDbMetadataRoleAndLocalCountersDespiteChangedEmailOrMissingName() throws Exception {
        String sub=UUID.randomUUID().toString(), email=email();
        mvc.perform(post("/api/v1/auth/google").contentType("application/json").content(body(sub,email,"Tên đã lưu"))).andExpect(status().isOk());
        jdbc.update("UPDATE users SET role='ADMIN',failed_login_attempts=5,locked_until=? WHERE provider_user_id=?",
                java.sql.Timestamp.from(GoogleTokenFixture.NOW.plusSeconds(300)),sub);
        mvc.perform(post("/api/v1/auth/google").contentType("application/json").content(body(sub,email(),null)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.fullName").value("Tên đã lưu")).andExpect(jsonPath("$.user.role").value("ADMIN"));
        var row=jdbc.queryForMap("SELECT failed_login_attempts,locked_until,last_login_at FROM users WHERE provider_user_id=?",sub);
        assertThat(row.get("failed_login_attempts")).isEqualTo(5);
        assertThat(((java.sql.Timestamp)row.get("locked_until")).toInstant()).isEqualTo(GoogleTokenFixture.NOW.plusSeconds(300));
        assertThat(((java.sql.Timestamp)row.get("last_login_at")).toInstant()).isEqualTo(GoogleTokenFixture.NOW);
    }
    @Test void rejectsLocalAndOtherGoogleEmailCollisionWithoutLinkingOrIssuingTokens() throws Exception {
        String local=email();
        mvc.perform(post("/api/v1/auth/register").contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("email",local,"password","password","fullName","Local")))).andExpect(status().isCreated());
        String google=email();
        mvc.perform(post("/api/v1/auth/google").contentType("application/json").content(body(UUID.randomUUID().toString(),google,"Google"))).andExpect(status().isOk());
        long before=tokens.count();
        for(String email:new String[]{local,google}) mvc.perform(post("/api/v1/auth/google").contentType("application/json")
                .content(body(UUID.randomUUID().toString(),email,"Other"))).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
        assertThat(tokens.count()).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT auth_provider FROM users WHERE email=?",String.class,local)).isEqualTo("LOCAL");
        assertThat(jdbc.queryForObject("SELECT password_hash IS NOT NULL FROM users WHERE email=?",Boolean.class,local)).isTrue();
    }
    @Test void manualLockRejectsVerifiedIdentityWithoutSessionOrAccountMutation() throws Exception {
        String sub=UUID.randomUUID().toString(),email=email();
        mvc.perform(post("/api/v1/auth/google").contentType("application/json").content(body(sub,email,"A"))).andExpect(status().isOk());
        jdbc.update("UPDATE users SET status='LOCKED',last_login_at=NULL WHERE provider_user_id=?",sub);
        long before=tokens.count();
        mvc.perform(post("/api/v1/auth/google").contentType("application/json").content(body(sub,email,"B")))
                .andExpect(status().isLocked()).andExpect(jsonPath("$.code").value("AUTH_ACCOUNT_LOCKED"));
        assertThat(tokens.count()).isEqualTo(before);
        assertThat(jdbc.queryForMap("SELECT last_login_at FROM users WHERE provider_user_id=?",sub).get("last_login_at")).isNull();
    }
    @Test void validatesBodyAndNewAccountNameWithoutDatabaseWrites() throws Exception {
        long before=tokens.count();
        for(String content:new String[]{"{}","{","{\"idToken\":\" \"}","{\"idToken\":null}"})
            mvc.perform(post("/api/v1/auth/google").contentType("application/json").content(content))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        for(String name:new String[]{null," ","x".repeat(101)}) mvc.perform(post("/api/v1/auth/google").contentType("application/json")
                .content(body(UUID.randomUUID().toString(),email(),name))).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/v1/auth/google").contentType("application/json").content("{\"idToken\":\"invalid\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        assertThat(tokens.count()).isEqualTo(before);
    }
    @Test void concurrentSameSubjectResolvesOneUserAndTwoIndependentSessions() throws Exception {
        String sub=UUID.randomUUID().toString(),email=email(),body=body(sub,email,"A");
        concurrent(body,body,200,200);
        UUID id=jdbc.queryForObject("SELECT id FROM users WHERE provider_user_id=?",UUID.class,sub);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE provider_user_id=?",Integer.class,sub)).isEqualTo(1);
        assertThat(tokens.findByUserId(id)).hasSize(2);
    }
    @Test void concurrentDifferentSubjectsSameEmailCannotCreateOrLinkSecondUser() throws Exception {
        String email=email();
        concurrent(body(UUID.randomUUID().toString(),email,"A"),body(UUID.randomUUID().toString(),email,"B"),200,409);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE email=?",Integer.class,email)).isEqualTo(1);
        UUID id=jdbc.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,email);
        assertThat(tokens.findByUserId(id)).hasSize(1);
    }
    void concurrent(String first,String second,int a,int b) throws Exception {
        var barrier=new CyclicBarrier(2);
        try(var executor=Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Integer> one=()-> {barrier.await(10,TimeUnit.SECONDS); return mvc.perform(post("/api/v1/auth/google").contentType("application/json").content(first)).andReturn().getResponse().getStatus();};
            java.util.concurrent.Callable<Integer> two=()-> {barrier.await(10,TimeUnit.SECONDS); return mvc.perform(post("/api/v1/auth/google").contentType("application/json").content(second)).andReturn().getResponse().getStatus();};
            var x=executor.submit(one);var y=executor.submit(two);
            assertThat(java.util.List.of(x.get(30,TimeUnit.SECONDS),y.get(30,TimeUnit.SECONDS))).containsExactlyInAnyOrder(a,b);
        }
    }
}
