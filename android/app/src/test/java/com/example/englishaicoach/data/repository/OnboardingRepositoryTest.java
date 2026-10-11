package com.example.englishaicoach.data.repository;

import static org.junit.Assert.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.OnboardingApi;
import com.example.englishaicoach.data.dto.OnboardingDtos;
import com.example.englishaicoach.domain.model.OnboardingData.*;
import com.example.englishaicoach.domain.repository.OnboardingRepository;
import com.google.gson.*;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class OnboardingRepositoryTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    static final String OLD = "00000000-0000-0000-0000-000000000002";
    static final String PROFILE = "{\"fullName\":\"Tên\",\"dailyLearningMinutes\":20,\"timezone\":\"UTC\",\"avatarUrl\":\"https://example.test/a\"}";
    static final String SELECTED = "{\"goals\":[{\"id\":\"" + ID + "\",\"name\":\"Du lịch\",\"isPrimary\":false}]}";
    MockWebServer server; SessionManager sessions; RetrofitOnboardingRepository repository;
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.start();
        sessions = new SessionManager(new TokenStore() { TokenSession value; public TokenSession read(){return value;} public void write(TokenSession v){value=v;} });
        sessions.start("access-A", "refresh-A");
        OkHttpClient http = new OkHttpClient.Builder().retryOnConnectionFailure(false).addInterceptor(new AuthInterceptor(sessions))
                .addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions)).build();
        OnboardingApi api = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .callFactory(new SessionBindingCallFactory(http,sessions)).addConverterFactory(GsonConverterFactory.create()).build().create(OnboardingApi.class);
        repository = new RetrofitOnboardingRepository(sessions,api,new ApiErrorParser());
    }
    @After public void cleanup() throws Exception { server.shutdown(); }
    MockResponse body(String json) { return new MockResponse().setBody(json); }
    OnboardingRepository.Result await(boolean load, boolean skip) throws Exception {
        CountDownLatch done=new CountDownLatch(1); AtomicReference<OnboardingRepository.Result> result=new AtomicReference<>();
        OnboardingRepository.Callback callback=value->{result.set(value);done.countDown();};
        if(load) repository.load(callback); else repository.save(new Profile("Tên",20,"UTC","https://example.test/a"),new Selection(List.of(ID),null),skip,callback);
        assertTrue(done.await(6,TimeUnit.SECONDS)); return result.get();
    }
    void user() { server.enqueue(body("{\"fullName\":\"Tên\"}")); }
    void catalog() { server.enqueue(body("[{\"id\":\""+ID+"\",\"name\":\"Du lịch\"}]")); }
    @Test public void missingProfileHasNoInventedTimeOrZoneAndIncludesInactiveSelection() throws Exception {
        user(); server.enqueue(new MockResponse().setResponseCode(404)); catalog();
        server.enqueue(body("{\"goals\":[{\"id\":\""+OLD+"\",\"name\":\"Cũ\",\"isPrimary\":true}]}"));
        var result=await(true,false); assertTrue(result.success);assertNull(result.snapshot.profile.timezone);assertNull(result.snapshot.profile.dailyLearningMinutes);
        assertEquals("Tên",result.snapshot.profile.fullName);assertFalse(result.snapshot.goals.get(1).active);
        assertEquals(OLD,result.snapshot.selection.primaryGoalId);
        assertEquals("/api/v1/users/me",server.takeRequest().getPath());assertEquals("/api/v1/users/me/profile",server.takeRequest().getPath());
        assertEquals("/api/v1/goals",server.takeRequest().getPath());assertEquals("/api/v1/users/me/goals",server.takeRequest().getPath());
    }
    @Test public void saveUsesTwoExactBodiesPreservesAvatarAndNoClientCefrOrEvent() throws Exception {
        server.enqueue(body(PROFILE));server.enqueue(body(SELECTED));assertTrue(await(false,false).success);
        RecordedRequest profile=server.takeRequest(),goals=server.takeRequest();
        assertEquals("PUT",profile.getMethod());assertEquals("/api/v1/users/me/profile",profile.getPath());
        JsonObject p=JsonParser.parseString(profile.getBody().readUtf8()).getAsJsonObject();assertEquals(4,p.size());assertEquals("https://example.test/a",p.get("avatarUrl").getAsString());
        assertEquals("Bearer access-A",goals.getHeader("Authorization"));assertEquals("/api/v1/users/me/goals",goals.getPath());
        JsonObject g=JsonParser.parseString(goals.getBody().readUtf8()).getAsJsonObject();assertEquals(1,g.size());assertFalse(g.has("primaryGoalId"));
    }
    @Test public void goalsFailureRecordsPartialAndExplicitRetrySendsOnlyGoals() throws Exception {
        server.enqueue(body(PROFILE));server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"code\":\"VALIDATION_ERROR\"}"));
        var result=await(false,false);assertFalse(result.success);assertTrue(result.profileSaved);assertEquals("VALIDATION_ERROR",result.error.code);
        server.enqueue(body(SELECTED));assertTrue(await(false,true).success);assertEquals(3,server.getRequestCount());
        server.takeRequest();server.takeRequest();assertEquals("/api/v1/users/me/goals",server.takeRequest().getPath());
    }
    @Test public void profileFailureDoesNotSubmitGoals() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(400));var result=await(false,false);
        assertFalse(result.profileSaved);assertEquals(1,server.getRequestCount());assertEquals(ApiError.Kind.VALIDATION,result.error.kind);
    }
    @Test public void forbiddenReadStopsWithoutTreatingAsMissingProfile() throws Exception {
        user();server.enqueue(new MockResponse().setResponseCode(403));var result=await(true,false);
        assertFalse(result.success);assertEquals(ApiError.Kind.FORBIDDEN,result.error.kind);assertEquals(2,server.getRequestCount());
    }
    @Test public void strictProfileRejectsCoercedNumbersAndBadSelection() {
        for(String bad:List.of(PROFILE.replace("20","\"20\""),PROFILE.replace("20","20.5"),PROFILE.replace("20","181"))) {
            try{OnboardingDtos.profile(JsonParser.parseString(bad));fail();}catch(RuntimeException expected){}
        }
        try{OnboardingDtos.selection(JsonParser.parseString(SELECTED.replace("false","\"false\"")),new ArrayList<>());fail();}catch(RuntimeException expected){}
    }
    @Test public void malformedProfile200DoesNotBlindlyContinueGoals() throws Exception {
        server.enqueue(body(PROFILE.replace("20","\"20\"")));var result=await(false,false);
        assertFalse(result.success);assertFalse(result.profileSaved);assertEquals(1,server.getRequestCount());
    }
    @Test public void accountSwitchBetweenStagesCannotSubmitOldGoalsOrPublish() throws Exception {
        CountDownLatch received=new CountDownLatch(1),release=new CountDownLatch(1),callback=new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher(){public MockResponse dispatch(RecordedRequest request)throws InterruptedException{received.countDown();release.await(5,TimeUnit.SECONDS);return body(PROFILE);}});
        repository.save(new Profile("Tên",20,"UTC","https://example.test/a"),new Selection(List.of(ID),null),false,r->callback.countDown());
        assertTrue(received.await(5,TimeUnit.SECONDS));sessions.start("access-B","refresh-B");release.countDown();
        assertFalse(callback.await(500,TimeUnit.MILLISECONDS));assertEquals(1,server.getRequestCount());assertEquals("access-B",sessions.current().accessToken());
    }
    @Test public void cancelSuppressesLateLoadAndRemainingStages() throws Exception {
        CountDownLatch received=new CountDownLatch(1),release=new CountDownLatch(1),callback=new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher(){public MockResponse dispatch(RecordedRequest request)throws InterruptedException{received.countDown();release.await(5,TimeUnit.SECONDS);return body("{\"fullName\":\"Tên\"}");}});
        var op=repository.load(r->callback.countDown());assertTrue(received.await(5,TimeUnit.SECONDS));op.cancel();release.countDown();
        assertFalse(callback.await(300,TimeUnit.MILLISECONDS));assertEquals(1,server.getRequestCount());
    }
}
