package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(GoogleLoginIntegrationTests.Configuration.class)
@TestPropertySource(properties={"app.jwt.secret=","app.jwt.refresh-token-expiration=7d","app.google-identity.client-id=fixture-server.apps.googleusercontent.com"})
class GoogleLoginRollbackIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired ObjectMapper mapper;
    @Test void failedTokenIssuanceRollsBackAccountCreation() throws Exception {
        String sub=UUID.randomUUID().toString(),email=UUID.randomUUID()+"@example.test";
        long before=tokens.count();
        mvc.perform(post("/api/v1/auth/google").contentType("application/json")
                .content(mapper.writeValueAsString(java.util.Map.of("idToken",GoogleTokenFixture.token(sub,email,"A")))))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        assertThat(users.findByEmail(email)).isEmpty(); assertThat(tokens.count()).isEqualTo(before);
    }
    @Test void failedTokenIssuanceRollsBackExistingLastLoginUpdate() throws Exception {
        String sub=UUID.randomUUID().toString(),email=UUID.randomUUID()+"@example.test";
        jdbc.update("INSERT INTO users(id,email,auth_provider,provider_user_id,full_name,role,status,failed_login_attempts,created_at,updated_at) VALUES (?,?,'GOOGLE',?,'A','USER','ACTIVE',0,now(),now())",UUID.randomUUID(),email,sub);
        long before=tokens.count();
        mvc.perform(post("/api/v1/auth/google").contentType("application/json")
                .content(mapper.writeValueAsString(java.util.Map.of("idToken",GoogleTokenFixture.token(sub,email,"A")))))
                .andExpect(status().isInternalServerError());
        assertThat(jdbc.queryForMap("SELECT last_login_at FROM users WHERE provider_user_id=?",sub).get("last_login_at")).isNull();
        assertThat(tokens.count()).isEqualTo(before);
    }
}
