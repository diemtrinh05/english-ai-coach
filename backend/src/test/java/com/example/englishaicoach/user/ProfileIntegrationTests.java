package com.example.englishaicoach.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.englishaicoach.auth.*;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(ProfileIntegrationTests.TimeConfiguration.class)
class ProfileIntegrationTests extends PostgreSqlIntegrationTestSupport {
    static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final String SECRET = randomSecret();
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService access;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @DynamicPropertySource static void config(DynamicPropertyRegistry r) { r.add("app.jwt.secret", () -> SECRET); }
    User user() { return users.saveAndFlush(User.local(UUID.randomUUID()+"@example.test", "password", "Người học", passwords)); }
    String jwt(User u) { return access.issue(u.getId(), UserRole.USER); }
    Map<String,Object> body(String name,int minutes,String zone) {
        return new LinkedHashMap<>(Map.of("fullName",name,"dailyLearningMinutes",minutes,"timezone",zone));
    }
    org.springframework.test.web.servlet.ResultActions save(String token,Map<String,Object> body) throws Exception {
        return mvc.perform(put("/api/v1/users/me/profile").header("Authorization","Bearer "+token)
                .contentType("application/json").content(mapper.writeValueAsString(body)));
    }
    @Test void userSummaryAndMissingProfileAreReadOnlyAndContainNoSecrets() throws Exception {
        User u=user(); String token=jwt(u);
        var response=mvc.perform(get("/api/v1/users/me").header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(u.getId().toString()))
                .andExpect(jsonPath("$.fullName").value("Người học")).andExpect(jsonPath("$.role").value("USER"))
                .andReturn().getResponse();
        assertThat(mapper.readTree(response.getContentAsString()).propertyNames()).containsExactlyInAnyOrder("id","email","fullName","role","status");
        assertThat(response.getContentAsString()).doesNotContain(token,"passwordHash","refreshToken");
        mvc.perform(get("/api/v1/users/me/profile").header("Authorization","Bearer "+token))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_profiles WHERE user_id=?",Integer.class,u.getId())).isZero();
    }
    @Test void createsThenReplacesEditableFieldsAndPreservesCefrAndCreatedTimestamp() throws Exception {
        User u=user();String token=jwt(u);var first=body("  Tên giữ nguyên  ",5,"UTC");first.put("avatarUrl","ảnh");
        save(token,first).andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("  Tên giữ nguyên  "))
                .andExpect(jsonPath("$.currentCefrLevel").doesNotExist()).andExpect(jsonPath("$.avatarUrl").value("ảnh"));
        UUID level=jdbc.queryForObject("SELECT id FROM cefr_levels WHERE code='A2'",UUID.class);
        Instant past=NOW.minusSeconds(999);
        jdbc.update("UPDATE user_profiles SET current_cefr_level_id=?,created_at=? WHERE user_id=?",level,java.sql.Timestamp.from(past),u.getId());
        var next=body("Tên mới",180,"Asia/Ho_Chi_Minh");next.put("userId",UUID.randomUUID());next.put("role","ADMIN");next.put("currentCefrLevelId",UUID.randomUUID());
        save(token,next).andExpect(status().isOk()).andExpect(jsonPath("$.avatarUrl").isEmpty())
                .andExpect(jsonPath("$.currentCefrLevel.id").value(level.toString())).andExpect(jsonPath("$.currentCefrLevel.code").value("A2"));
        assertThat(users.findById(u.getId()).orElseThrow().getRole()).isEqualTo(UserRole.USER);
        var row=jdbc.queryForMap("SELECT created_at,updated_at,current_cefr_level_id FROM user_profiles WHERE user_id=?",u.getId());
        assertThat(((java.sql.Timestamp)row.get("created_at")).toInstant()).isEqualTo(past);
        assertThat(((java.sql.Timestamp)row.get("updated_at")).toInstant()).isEqualTo(NOW);
        next.put("avatarUrl",null);save(token,next).andExpect(status().isOk()).andExpect(jsonPath("$.avatarUrl").isEmpty());
        mvc.perform(get("/api/v1/users/me/profile").header("Authorization","Bearer "+token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLearningMinutes").value(180)).andExpect(jsonPath("$.fullName").value("Tên mới"));
    }
    @Test void validationRejectsMissingNullBlankOutOfRangeAndInvalidTimezoneWithoutMutation() throws Exception {
        User u=user();String token=jwt(u);var invalid=new ArrayList<Map<String,Object>>();
        for(String field:List.of("fullName","dailyLearningMinutes","timezone")) {
            var missing=body("Tên",20,"UTC");missing.remove(field);invalid.add(missing);
            var nil=body("Tên",20,"UTC");nil.put(field,null);invalid.add(nil);
        }
        for(String name:List.of("","   ","x".repeat(101)))invalid.add(body(name,20,"UTC"));
        for(int minutes:List.of(4,181))invalid.add(body("Tên",minutes,"UTC"));
        for(String zone:List.of("","  "," UTC","UTC ","+07:00","UTC+07:00","EST","Invalid/Zone","x".repeat(51)))invalid.add(body("Tên",20,zone));
        for(Object minutes:List.of(20.5,"20",true)) {var typed=body("Tên",20,"UTC");typed.put("dailyLearningMinutes",minutes);invalid.add(typed);}
        for(String field:List.of("fullName","avatarUrl","timezone")) {var typed=body("Tên",20,"UTC");typed.put(field,123);invalid.add(typed);}
        for(var b:invalid)save(token,b).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(put("/api/v1/users/me/profile").header("Authorization","Bearer "+token).contentType("application/json").content("{broken"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/users/me/profile").header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
        assertThat(users.findById(u.getId()).orElseThrow().getFullName()).isEqualTo("Người học");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_profiles WHERE user_id=?",Integer.class,u.getId())).isZero();
        save(token,body("x".repeat(100),5,"America/New_York")).andExpect(status().isOk());
    }
    @Test void bearerAndOwnershipApplyToUserAndAdminAndUnknownUser() throws Exception {
        User owner=user(); User caller=user();String token=access.issue(caller.getId(),UserRole.ADMIN);
        var request=body("Riêng caller",20,"UTC");request.put("userId",owner.getId());save(token,request).andExpect(status().isOk());
        assertThat(users.findById(owner.getId()).orElseThrow().getFullName()).isEqualTo("Người học");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_profiles WHERE user_id=?",Integer.class,owner.getId())).isZero();
        for(String path:List.of("/api/v1/users/me","/api/v1/users/me/profile")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
            mvc.perform(get(path).header("Authorization","Bearer "+access.issue(UUID.randomUUID(),UserRole.USER)))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
        save(access.issue(UUID.randomUUID(),UserRole.USER),body("Tên",20,"UTC")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/users/me/profile").contentType("application/json").content(mapper.writeValueAsString(request))).andExpect(status().isUnauthorized());
    }
    @Test void concurrentFirstWritesAreAtomicAndCreateOneProfile() throws Exception {
        User u=user();String token=jwt(u);CyclicBarrier barrier=new CyclicBarrier(2);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var tasks=new ArrayList<Callable<Void>>();
            for(int i=0;i<2;i++) { final int n=i;tasks.add(()->{barrier.await(10,TimeUnit.SECONDS);
                save(token,body("Tên"+n,20+n,n==0?"UTC":"Asia/Tokyo")).andExpect(status().isOk())
                        .andExpect(jsonPath("$.fullName").value("Tên"+n)).andExpect(jsonPath("$.dailyLearningMinutes").value(20+n));return null;}); }
            for(var result:pool.invokeAll(tasks))result.get(30,TimeUnit.SECONDS);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_profiles WHERE user_id=?",Integer.class,u.getId())).isEqualTo(1);
        var row=jdbc.queryForMap("SELECT u.full_name,p.daily_learning_minutes FROM users u JOIN user_profiles p ON p.user_id=u.id WHERE u.id=?",u.getId());
        assertThat(row.get("full_name")).isEqualTo("Tên"+(((Integer)row.get("daily_learning_minutes"))-20));
    }
    @Test void profileStorageFailureRollsBackNameAndProfileAndAllowsRetry() throws Exception {
        User u=user();String token=jwt(u);save(token,body("Tên ban đầu",20,"UTC")).andExpect(status().isOk());
        jdbc.execute("CREATE FUNCTION reject_profile_test() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$");
        jdbc.execute("CREATE TRIGGER reject_profile_test BEFORE INSERT OR UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION reject_profile_test()");
        try {save(token,body("Không lưu",30,"Asia/Tokyo")).andExpect(status().isInternalServerError());
            assertThat(users.findById(u.getId()).orElseThrow().getFullName()).isEqualTo("Tên ban đầu");
            assertThat(jdbc.queryForObject("SELECT daily_learning_minutes FROM user_profiles WHERE user_id=?",Integer.class,u.getId())).isEqualTo(20);
        } finally {jdbc.execute("DROP TRIGGER reject_profile_test ON user_profiles");jdbc.execute("DROP FUNCTION reject_profile_test()");}
        save(token,body("Lưu lại",30,"Asia/Tokyo")).andExpect(status().isOk());
    }
    private static String randomSecret(){byte[] b=new byte[32];new SecureRandom().nextBytes(b);return Base64.getEncoder().encodeToString(b);}
    @TestConfiguration static class TimeConfiguration {@Bean @Primary Clock clock(){return Clock.fixed(NOW,ZoneOffset.UTC);}}
}
