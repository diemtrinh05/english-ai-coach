package com.example.englishaicoach.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.auth.JwtAccessTokenService;
import com.example.englishaicoach.auth.PasswordHashService;
import com.example.englishaicoach.auth.User;
import com.example.englishaicoach.auth.UserRepository;
import com.example.englishaicoach.auth.UserRole;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(VocabularyProgressIntegrationTests.TimeConfiguration.class)
class VocabularyProgressIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-17T10:20:30Z");
    private static final String SECRET = randomSecret();
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService tokens;
    @Autowired ObjectMapper mapper;
    private final List<UUID> ownedUsers = new ArrayList<>();
    private final List<UUID> ownedWords = new ArrayList<>();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) { registry.add("app.jwt.secret", () -> SECRET); registry.add("app.srs.initial-ease-factor", () -> "2.75"); }

    @AfterEach
    void cleanup() {
        for (UUID id : ownedUsers) {
            jdbc.update("DELETE FROM user_vocabulary_progress WHERE user_id=?", id);
            jdbc.update("DELETE FROM users WHERE id=?", id);
        }
        for (UUID id : ownedWords) {
            jdbc.update("DELETE FROM vocabulary_topics WHERE vocabulary_id=?", id);
            jdbc.update("DELETE FROM vocabulary WHERE id=?", id);
        }
    }

    @Test
    void missingProgressReturnsConfiguredNewSnapshotWithoutCreatingState() throws Exception {
        UUID user=user(), word=word(true);
        for(int i=0;i<2;i++) {
            var body=mapper.readTree(request(user,word,"").andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
            assertThat(body.properties().stream().map(java.util.Map.Entry::getKey).toList()).containsExactlyInAnyOrder(
                    "vocabularyId","status","easeFactor","intervalDays","repetitions","nextReviewAt","correctCount","incorrectCount");
            assertThat(body.get("status").asText()).isEqualTo("NEW");
            assertThat(body.get("easeFactor").decimalValue()).isEqualByComparingTo("2.75");
            assertThat(body.get("nextReviewAt").isNull()).isTrue();
            for(String field:List.of("intervalDays","repetitions","correctCount","incorrectCount")) assertThat(body.get(field).asInt()).isZero();
        }
        assertNoSideEffects(user);
    }

    @Test
    void returnsEveryStoredStatusAndExactStateWithoutModification() throws Exception {
        UUID user=user();
        for(String state:List.of("NEW","LEARNING","REVIEWING","MASTERED")) {
            UUID word=word(true); progress(user,word,state,state.equals("NEW")?null:NOW);
            jdbc.update("UPDATE user_vocabulary_progress SET ease_factor=1.87,interval_days=17,repetitions=3,correct_count=5,incorrect_count=2 WHERE user_id=? AND vocabulary_id=?",user,word);
            var before=jdbc.queryForMap("SELECT * FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?",user,word);
            var body=mapper.readTree(request(user,word,"").andExpect(status().isOk())
                    .andExpect(jsonPath("$.vocabularyId").value(word.toString())).andExpect(jsonPath("$.status").value(state))
                    .andExpect(jsonPath("$.easeFactor").value(1.87)).andExpect(jsonPath("$.intervalDays").value(17))
                    .andExpect(jsonPath("$.repetitions").value(3)).andExpect(jsonPath("$.correctCount").value(5))
                    .andExpect(jsonPath("$.incorrectCount").value(2)).andReturn().getResponse().getContentAsString());
            if(state.equals("NEW")) assertThat(body.get("nextReviewAt").isNull()).isTrue();
            else assertThat(body.get("nextReviewAt").asText()).isEqualTo(NOW.toString());
            assertThat(jdbc.queryForMap("SELECT * FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?",user,word)).isEqualTo(before);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=?",Integer.class,user)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM streaks WHERE user_id=?",Integer.class,user)).isZero();
    }

    @Test
    void isolatesCallersAndDoesNotExposeAnotherUsersProgress() throws Exception {
        UUID owner=user(), other=user(), word=word(true); progress(owner,word,"MASTERED",NOW);
        request(owner,word,"").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("MASTERED"));
        request(other,word,"").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NEW"));
        request(other,word,"?userId="+owner).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertNoSideEffects(other);
    }

    @Test
    void missingAndInactiveVocabularyAreNotFoundEvenWithStoredProgress() throws Exception {
        UUID user=user(), hidden=word(false); progress(user,hidden,"REVIEWING",NOW);
        for(UUID word:List.of(hidden,UUID.randomUUID())) request(user,word,"").andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void requiresAuthenticationUserRoleAndExistingUser() throws Exception {
        UUID word=word(true), user=user();
        mvc.perform(get(path(word))).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get(path(word)).header("Authorization","Bearer "+tokens.issue(user,UserRole.ADMIN)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        request(UUID.randomUUID(),word,"").andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        assertNoSideEffects(user);
    }

    @Test
    void rejectsMalformedUuidAndEveryUndeclaredQuery() throws Exception {
        UUID user=user(), word=word(true);
        for(String id:List.of("wrong","1-1-1-1-1","00000000-0000-0000-0000-00000000000g"))
            mvc.perform(get("/api/v1/vocabulary/"+id+"/progress").header("Authorization","Bearer "+tokens.issue(user,UserRole.USER)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        for(String query:List.of("?limit=1","?userId="+user,"?x=1&x=2","?x="))
            request(user,word,query).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertNoSideEffects(user);
    }

    private void assertNoSideEffects(UUID user) {
        for(String table:List.of("user_vocabulary_progress","user_profiles","xp_logs","streaks","idempotency_keys"))
            assertThat(jdbc.queryForObject("SELECT count(*) FROM "+table+" WHERE user_id=?",Integer.class,user)).as(table).isZero();
    }
    private static String path(UUID word) {return "/api/v1/vocabulary/"+word+"/progress";}
    private org.springframework.test.web.servlet.ResultActions request(UUID user,UUID word,String query) throws Exception {
        return mvc.perform(get(path(word)+query).header("Authorization","Bearer "+tokens.issue(user,UserRole.USER)));
    }
    private UUID user() {
        User user=users.saveAndFlush(User.local(UUID.randomUUID()+"@example.test","password","Người học",passwords));
        ownedUsers.add(user.getId()); return user.getId();
    }
    private UUID word(boolean active) {
        UUID id=UUID.randomUUID(); ownedWords.add(id);
        jdbc.update("INSERT INTO vocabulary(id,word,cefr_level_id,source,is_active,created_at,updated_at) VALUES (?,?,(SELECT id FROM cefr_levels LIMIT 1),'MANUAL',?,now(),now())",id,id.toString(),active);
        return id;
    }
    private void progress(UUID user, UUID word,String state, Instant next) {
        jdbc.update("INSERT INTO user_vocabulary_progress(id,user_id,vocabulary_id,status,ease_factor,interval_days,repetitions,next_review_at,correct_count,incorrect_count,version,created_at,updated_at) VALUES (?,?,?, ?,2.5,1,0,?,0,0,0,now(),now())",UUID.randomUUID(),user,word,state,next==null?null:Timestamp.from(next));
    }
    private static String randomSecret() {byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);return Base64.getEncoder().encodeToString(bytes);}
    @TestConfiguration static class TimeConfiguration {
        @Bean @Primary Clock clock() {return Clock.fixed(NOW,ZoneOffset.UTC);}
    }
}
