package com.example.englishaicoach.learning;

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
@Import(ReviewQueueIntegrationTests.TimeConfiguration.class)
class ReviewQueueIntegrationTests extends PostgreSqlIntegrationTestSupport {
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
    private final List<UUID> ownedTopics = new ArrayList<>();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) { registry.add("app.jwt.secret", () -> SECRET); }

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
        for (UUID id : ownedTopics.reversed()) jdbc.update("DELETE FROM topics WHERE id=?", id);
    }

    @Test
    void exactClockCutoffAndAllStatusesWithStableTieOrderAndNoRisk() throws Exception {
        UUID user = user();
        UUID other = user();
        List<UUID> tied = new ArrayList<>();
        for (String state : List.of("NEW", "LEARNING", "REVIEWING", "MASTERED")) {
            UUID word = word(true); progress(user, word, state, NOW); tied.add(word);
        }
        tied.sort(java.util.Comparator.comparing(UUID::toString));
        UUID earlier = word(true); progress(user, earlier, "REVIEWING", NOW.minusSeconds(1));
        progress(user, word(true), "REVIEWING", NOW.plusNanos(1000));
        progress(user, word(true), "NEW", null);
        progress(user, word(false), "REVIEWING", NOW.minusSeconds(100));
        progress(other, word(true), "REVIEWING", NOW.minusSeconds(100));
        var response = request(user, "").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(5))
                .andExpect(jsonPath("$.items[0].vocabularyId").value(earlier.toString()))
                .andExpect(jsonPath("$.items[0].forgettingRisk").doesNotExist()).andReturn().getResponse();
        var body = mapper.readTree(response.getContentAsString());
        for (int i=0; i<tied.size(); i++) {
            assertThat(body.get("items").get(i+1).get("vocabularyId").asText()).isEqualTo(tied.get(i).toString());
            assertThat(body.get("items").get(i+1).get("nextReviewAt").asText()).isEqualTo(NOW.toString());
        }
        assertThat(body.properties().stream().map(java.util.Map.Entry::getKey).toList()).containsExactlyInAnyOrder("items", "count");
    }

    @Test
    void limitsDefaultAndMaximumAndCountAfterLimit() throws Exception {
        UUID user = user();
        for (int i=0; i<105; i++) progress(user, word(true), "LEARNING", NOW.minusSeconds(i));
        request(user, "").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(50));
        request(user, "?limit=1").andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.count").value(1));
        request(user, "?limit=100").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(100));
    }

    @Test
    void topicFilterIsExactActiveAndDoesNotDuplicateOrIncludeDescendants() throws Exception {
        UUID user = user();
        UUID parent = topic(true, null); UUID child = topic(true, parent); UUID inactive = topic(false, null);
        UUID direct = word(true); UUID descendant = word(true); UUID hidden = word(true);
        for (UUID word : List.of(direct, descendant, hidden)) progress(user, word, "REVIEWING", NOW);
        link(direct,parent); link(direct,child); link(descendant,child); link(hidden,inactive);
        request(user, "?topicId="+parent).andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.items[0].vocabularyId").value(direct.toString()));
        request(user, "?topicId="+inactive).andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));
        request(user, "?topicId="+UUID.randomUUID()).andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));
        request(user, "").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void malformedRepeatedAndUnknownQueryIsRejected() throws Exception {
        UUID user = user();
        for (String query : List.of("?limit=0", "?limit=101", "?limit=-1", "?limit=1.5", "?limit=abc", "?limit=",
                "?limit=99999999999999999", "?limit=1&limit=2", "?topicId=", "?topicId=1-1-1-1-1",
                "?topicId=wrong", "?topicId="+UUID.randomUUID()+"&topicId="+UUID.randomUUID(),
                "?userId="+UUID.randomUUID(), "?sort=word")) {
            request(user,query).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }

    @Test
    void authenticationRoleUnknownUserAndEmptyQueue() throws Exception {
        mvc.perform(get("/api/v1/learning/reviews")).andExpect(status().isUnauthorized());
        UUID user = user();
        mvc.perform(get("/api/v1/learning/reviews").header("Authorization", "Bearer "+tokens.issue(user,UserRole.ADMIN)))
                .andExpect(status().isForbidden());
        request(UUID.randomUUID(), "").andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        request(user, "").andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty()).andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void readDoesNotMutateProgressOrLedgersAndReevaluatesClockEligibility() throws Exception {
        UUID user = user(); UUID word=word(true); progress(user,word,"MASTERED",NOW);
        var before=jdbc.queryForMap("SELECT * FROM user_vocabulary_progress WHERE user_id=?",user);
        long xp=jdbc.queryForObject("SELECT count(*) FROM xp_logs",Long.class);
        long events=jdbc.queryForObject("SELECT count(*) FROM idempotency_keys",Long.class);
        for(int i=0;i<2;i++) request(user, "").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1));
        assertThat(jdbc.queryForMap("SELECT * FROM user_vocabulary_progress WHERE user_id=?",user)).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs",Long.class)).isEqualTo(xp);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM idempotency_keys",Long.class)).isEqualTo(events);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM streaks WHERE user_id=?",Integer.class,user)).isZero();
        jdbc.update("UPDATE user_vocabulary_progress SET next_review_at=? WHERE user_id=?",Timestamp.from(NOW.plusSeconds(1)),user);
        request(user, "").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void explainSanityUsesCallerIndexesWithoutDisablingSequentialScan() throws Exception {
        UUID user=user(); UUID shared=word(true);
        // Dữ liệu đại diện: nhiều user, caller chọn lọc; không ép planner bằng enable_seqscan.
        String prefix=UUID.randomUUID()+"@perf-";
        jdbc.update("INSERT INTO users(id,email,full_name,auth_provider,role,status,created_at,updated_at) SELECT gen_random_uuid(), ? || g, 'Hiệu năng','LOCAL','USER','ACTIVE',now(),now() FROM generate_series(1,2000) g",prefix);
        List<UUID> background=jdbc.query("SELECT id FROM users WHERE email LIKE ?",(rs,row)->rs.getObject(1,UUID.class),prefix+"%");
        ownedUsers.addAll(background);
        for(UUID id: background) progress(id,shared,"REVIEWING",NOW);
        for(int i=0;i<10;i++) progress(user,word(true),"REVIEWING",NOW);
        UUID topic=topic(true,null);
        for(UUID word:ownedWords) link(word,topic);
        jdbc.execute("ANALYZE user_vocabulary_progress"); jdbc.execute("ANALYZE vocabulary");
        for(boolean filtered:List.of(false,true)) {
            Object[] args=filtered?new Object[]{user,Timestamp.from(NOW),topic,50}:new Object[]{user,Timestamp.from(NOW),50};
            String plan=String.join("\n",jdbc.queryForList("EXPLAIN (ANALYZE, BUFFERS) "+ReviewQueueRepository.QUERY
                    +(filtered?ReviewQueueRepository.TOPIC_FILTER:"")+ReviewQueueRepository.ORDER_LIMIT,String.class,args));
            System.out.println("BE-LEARN-004 EXPLAIN topic="+filtered+"\n"+plan);
            assertThat(plan).contains("Index", "user_vocabulary_progress");
        }
    }

    private org.springframework.test.web.servlet.ResultActions request(UUID user, String query) throws Exception {
        return mvc.perform(get("/api/v1/learning/reviews"+query).header("Authorization","Bearer "+tokens.issue(user,UserRole.USER)));
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
    private UUID topic(boolean active,UUID parent) {
        UUID id=UUID.randomUUID();ownedTopics.add(id);
        jdbc.update("INSERT INTO topics(id,name,is_active,parent_topic_id,created_at,updated_at) VALUES (?,?,?,?,now(),now())",id,id.toString(),active,parent);return id;
    }
    private void link(UUID word,UUID topic) {jdbc.update("INSERT INTO vocabulary_topics(vocabulary_id,topic_id) VALUES (?,?)",word,topic);}
    private static String randomSecret() {byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);return Base64.getEncoder().encodeToString(bytes);}
    @TestConfiguration static class TimeConfiguration {
        @Bean @Primary Clock clock() {return Clock.fixed(NOW,ZoneOffset.UTC);}
    }
}
