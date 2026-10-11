package com.example.englishaicoach.onboarding;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.englishaicoach.auth.*;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(GoalIntegrationTests.TimeConfiguration.class)
class GoalIntegrationTests extends PostgreSqlIntegrationTestSupport {
    static final Instant NOW=Instant.parse("2026-10-11T00:00:00Z");
    static final String SECRET=randomSecret();
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService access;
    @Autowired ObjectMapper mapper;
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) { r.add("app.jwt.secret",()->SECRET); }
    User user() { return users.saveAndFlush(User.local(UUID.randomUUID()+"@example.test","password","Người học",passwords)); }
    UUID goal(String name,boolean active) {
        UUID id=UUID.randomUUID();jdbc.update("INSERT INTO goals(id,name,is_active,created_at) VALUES(?,?,?,?)",id,name+UUID.randomUUID(),active,Timestamp.from(NOW));return id;
    }
    String jwt(User u) { return access.issue(u.getId(),UserRole.USER); }
    Map<String,Object> request(List<UUID> ids,UUID primary) {
        var b=new LinkedHashMap<String,Object>();b.put("goalIds",ids);b.put("primaryGoalId",primary);return b;
    }
    ResultActions save(String token,Object body) throws Exception {
        return mvc.perform(put("/api/v1/users/me/goals").header("Authorization","Bearer "+token)
                .contentType("application/json").content(mapper.writeValueAsString(body)));
    }
    List<Map<String,Object>> rows(UUID userId) {return jdbc.queryForList("SELECT id,goal_id,is_primary,created_at FROM user_goals WHERE user_id=? ORDER BY goal_id",userId);}

    @Test void publicCatalogActiveOnlySortedAndNullableDescription() throws Exception {
        UUID inactive=goal("ZZZ-inactive",false); UUID active=goal("ZZZ-active",true);
        var response=mvc.perform(get("/api/v1/goals")).andExpect(status().isOk()).andReturn().getResponse();
        var array=mapper.readTree(response.getContentAsString());assertThat(array.isArray()).isTrue();
        var names=new ArrayList<String>();var ids=new ArrayList<String>();
        for(var item:array) {names.add(item.get("name").asText());ids.add(item.get("id").asText());assertThat(item.propertyNames()).containsExactlyInAnyOrder("id","name","description");}
        assertThat(names).isSorted();assertThat(ids).contains(active.toString()).doesNotContain(inactive.toString());
        mvc.perform(get("/api/v1/goals").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
    }

    @Test void emptyOwnerReadDoesNotWriteAndMissingUserIsUnauthorized() throws Exception {
        User u=user();mvc.perform(get("/api/v1/users/me/goals").header("Authorization","Bearer "+jwt(u)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.goals").isEmpty());assertThat(rows(u.getId())).isEmpty();
        String missing=access.issue(UUID.randomUUID(),UserRole.USER);
        mvc.perform(get("/api/v1/users/me/goals").header("Authorization","Bearer "+missing)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        save(missing,request(List.of(goal("valid",true)),null)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/users/me/goals")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/users/me/goals")).andExpect(status().isUnauthorized());
    }

    @Test void replacementOptionalPrimaryRetryAndInactiveReadsPreserveRetainedMetadata() throws Exception {
        User u=user();String token=jwt(u);UUID a=goal("A",true),b=goal("B",true),c=goal("C",true);
        save(token,request(List.of(b,a),a)).andExpect(status().isOk()).andExpect(jsonPath("$.goals[0].id").value(a.toString())).andExpect(jsonPath("$.goals[0].isPrimary").value(true));
        var first=rows(u.getId());save(token,request(List.of(a,b),a)).andExpect(status().isOk());assertThat(rows(u.getId())).isEqualTo(first);
        var retained=first.stream().filter(r->r.get("goal_id").equals(b)).findFirst().orElseThrow();
        save(token,request(List.of(b,c),c)).andExpect(status().isOk());
        var after=rows(u.getId());assertThat(after).hasSize(2);
        var saved=after.stream().filter(r->r.get("goal_id").equals(b)).findFirst().orElseThrow();
        assertThat(saved.get("id")).isEqualTo(retained.get("id"));assertThat(saved.get("created_at")).isEqualTo(retained.get("created_at"));
        var absent=new LinkedHashMap<>(request(List.of(b),null));absent.remove("primaryGoalId");
        save(token,absent).andExpect(status().isOk()).andExpect(jsonPath("$.goals[0].isPrimary").value(false));
        save(token,request(List.of(b),null)).andExpect(status().isOk());
        jdbc.update("UPDATE goals SET is_active=false WHERE id=?",b);
        mvc.perform(get("/api/v1/users/me/goals").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.goals[0].id").value(b.toString()));
    }

    @Test void invalidRequestsHaveExactErrorsAndNoMutation() throws Exception {
        User u=user();String token=jwt(u);UUID active=goal("valid",true),inactive=goal("inactive",false);
        save(token,request(List.of(active),active)).andExpect(status().isOk());var before=rows(u.getId());
        var invalid=new ArrayList<Object>();
        invalid.add(Map.of());invalid.add(request(List.of(),null));invalid.add(request(List.of(active,active),null));
        invalid.add(request(List.of(active),UUID.randomUUID()));invalid.add(request(List.of(inactive),null));
        invalid.add(Map.of("goalIds",Arrays.asList(active,null)));invalid.add(Map.of("goalIds",List.of("invalid")));
        for(String value:List.of("1-1-1-1-1",Base64.getEncoder().encodeToString(new byte[16])," "+active)) {
            invalid.add(Map.of("goalIds",List.of(value)));invalid.add(Map.of("goalIds",List.of(active),"primaryGoalId",value));
        }
        invalid.add(Map.of("goalIds",List.of(123)));invalid.add(Map.of("goalIds","wrong"));
        var nil=request(List.of(active),null);nil.put("goalIds",null);invalid.add(nil);
        invalid.add(Map.of("goalIds",List.of(active),"primaryGoalId","bad"));
        for(Object body:invalid) {save(token,body).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));assertThat(rows(u.getId())).isEqualTo(before);}
        save(token,request(List.of(UUID.randomUUID()),null)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(put("/api/v1/users/me/goals").header("Authorization","Bearer "+token).contentType("application/json").content("{broken")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/users/me/goals").header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
        assertThat(rows(u.getId())).isEqualTo(before);
    }

    @Test void authenticatedIdentityOwnsSelectionsIncludingAdminAndIgnoresExtraUserId() throws Exception {
        User owner=user(),caller=user();UUID a=goal("owner",true),b=goal("caller",true);
        save(jwt(owner),request(List.of(a),a)).andExpect(status().isOk());var ownerRows=rows(owner.getId());
        var body=request(List.of(b),null);body.put("userId",owner.getId());
        String admin=access.issue(caller.getId(),UserRole.ADMIN);
        save(admin,body).andExpect(status().isOk()).andExpect(jsonPath("$.goals[0].id").value(b.toString()));
        mvc.perform(get("/api/v1/users/me/goals").header("Authorization","Bearer "+admin)).andExpect(status().isOk()).andExpect(jsonPath("$.goals[0].id").value(b.toString()));
        assertThat(rows(owner.getId())).isEqualTo(ownerRows);assertThat(rows(caller.getId())).hasSize(1);
    }

    @Test void concurrentFirstReplacementsAndPrimarySwitchesStayAtomic() throws Exception {
        User u=user();String token=jwt(u);UUID a=goal("A",true),b=goal("B",true),c=goal("C",true);
        race(token,request(List.of(a,b),a),request(List.of(b,c),c));
        var finalIds=rows(u.getId()).stream().map(r->(UUID)r.get("goal_id")).toList();
        assertThat(new HashSet<>(finalIds)).isIn(Set.of(a,b),Set.of(b,c));
        assertThat(rows(u.getId()).stream().filter(r->Boolean.TRUE.equals(r.get("is_primary"))).count()).isEqualTo(1);
        race(token,request(List.of(a,b),a),request(List.of(a,b),b));
        assertThat(rows(u.getId())).hasSize(2);assertThat(rows(u.getId()).stream().filter(r->Boolean.TRUE.equals(r.get("is_primary"))).count()).isEqualTo(1);
    }
    void race(String token,Object first,Object second) throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),go=new CountDownLatch(1);
        try {
            List<Future<MvcResult>> pending=new ArrayList<>();
            for(Object body:List.of(first,second))pending.add(pool.submit(()->{ready.countDown();assertThat(go.await(10,TimeUnit.SECONDS)).isTrue();return save(token,body).andExpect(status().isOk()).andReturn();}));
            assertThat(ready.await(10,TimeUnit.SECONDS)).isTrue();go.countDown();
            for(int i=0;i<pending.size();i++) {var result=pending.get(i).get(30,TimeUnit.SECONDS);var goals=mapper.readTree(result.getResponse().getContentAsString()).get("goals");assertThat(goals.size()).isEqualTo(2);
                @SuppressWarnings("unchecked") var expected=(Map<String,Object>)List.of(first,second).get(i);
                var actualIds=new HashSet<String>();for(var goal:goals)actualIds.add(goal.get("id").asText());
                @SuppressWarnings("unchecked") var expectedIds=(List<UUID>)expected.get("goalIds");
                assertThat(actualIds).isEqualTo(new HashSet<>(expectedIds.stream().map(UUID::toString).toList()));
                long primary=0;for(var goal:goals)if(goal.get("isPrimary").asBoolean())primary++;assertThat(primary).isEqualTo(1);}
        } finally {go.countDown();pool.shutdownNow();}
    }

    @Test void databaseUniquePairAndPartialPrimaryProtectFutureWritePaths() throws Exception {
        User u=user();UUID a=goal("A",true),b=goal("B",true);
        save(jwt(u),request(List.of(a),a)).andExpect(status().isOk());
        assertThatThrownBy(()->jdbc.update("INSERT INTO user_goals(id,user_id,goal_id,is_primary,created_at) VALUES(?,?,?,false,?)",UUID.randomUUID(),u.getId(),a,Timestamp.from(NOW))).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(()->jdbc.update("INSERT INTO user_goals(id,user_id,goal_id,is_primary,created_at) VALUES(?,?,?,true,?)",UUID.randomUUID(),u.getId(),b,Timestamp.from(NOW))).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(rows(u.getId())).hasSize(1);
    }

    @Test void storageFailureRollsBackRemovalAndPrimaryThenRetrySucceeds() throws Exception {
        User u=user();String token=jwt(u);UUID a=goal("A",true),b=goal("B",true);
        save(token,request(List.of(a),a)).andExpect(status().isOk());var before=rows(u.getId());
        jdbc.execute("CREATE FUNCTION goal001_fail() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.user_id='"+u.getId()+"'::uuid THEN RAISE EXCEPTION 'storage fixture'; END IF; RETURN NEW; END $$");
        jdbc.execute("CREATE TRIGGER goal001_failure BEFORE INSERT ON user_goals FOR EACH ROW EXECUTE FUNCTION goal001_fail()");
        try {save(token,request(List.of(b),b)).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));assertThat(rows(u.getId())).isEqualTo(before);}
        finally {jdbc.execute("DROP TRIGGER goal001_failure ON user_goals");jdbc.execute("DROP FUNCTION goal001_fail()");}
        save(token,request(List.of(b),b)).andExpect(status().isOk());assertThat(rows(u.getId())).hasSize(1);assertThat(rows(u.getId()).getFirst().get("goal_id")).isEqualTo(b);
    }
    static String randomSecret() {byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);return Base64.getEncoder().encodeToString(bytes);}
    @TestConfiguration static class TimeConfiguration {@Bean @Primary Clock goalClock() {return Clock.fixed(NOW,ZoneOffset.UTC);}}
}
