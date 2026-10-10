package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@TestPropertySource(properties="app.jwt.secret=")
class RefreshRollbackIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired RefreshTokenRepository tokens;
    @Autowired RefreshTokenHasher hasher; @Autowired PasswordHashService passwords;
    @Test void signingFailureRollsBackOldConsumptionAndCreatesNoNewSession() throws Exception {
        User u=users.saveAndFlush(User.local(UUID.randomUUID()+"@example.test","password","Người học",passwords));String raw=UUID.randomUUID().toString();
        tokens.saveAndFlush(RefreshToken.forRawToken(u.getId(),raw,Instant.now().plusSeconds(3600),null,hasher));
        mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content("{\"refreshToken\":\""+raw+"\"}")) .andExpect(status().isInternalServerError());
        var old=tokens.findByTokenHash(hasher.hash(raw)).orElseThrow();assertThat(old.getRevokedAt()).isNull();assertThat(old.getLastUsedAt()).isNull();assertThat(tokens.findByUserId(u.getId())).hasSize(1);
    }
}
