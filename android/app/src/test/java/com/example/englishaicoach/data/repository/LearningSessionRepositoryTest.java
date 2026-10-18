package com.example.englishaicoach.data.repository;

import static org.junit.Assert.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.*;
import com.example.englishaicoach.domain.repository.LearningSessionRepository;
import com.google.gson.*;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class LearningSessionRepositoryTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    static final String EVENT = "00000000-0000-0000-0000-000000000002";
    static final String USER = "{\"id\":\"user\",\"email\":\"user@example.test\",\"fullName\":\"Tên\",\"role\":\"USER\"}";
    static final String SESSION = "{\"sessionId\":\"" + ID + "\",\"sessionType\":\"MIXED\",\"startedAt\":\"2026-10-18T00:00:00Z\",\"endedAt\":null}";
    MockWebServer server; SessionManager sessions; RetrofitLearningSessionRepository repository; String owner;
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.start(); sessions = new SessionManager(new TokenStore() {
            TokenSession value; public TokenSession read() { return value; } public void write(TokenSession token) { value = token; }
        }); sessions.start("access-A", "refresh-A"); owner = sessions.current().sessionId();
        OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(false)
                .addInterceptor(new AuthInterceptor(sessions)).addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions)).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .callFactory(new SessionBindingCallFactory(client, sessions)).addConverterFactory(GsonConverterFactory.create()).build();
        repository = new RetrofitLearningSessionRepository(sessions, retrofit.create(LearningSessionApi.class), retrofit.create(VocabularyApi.class), new ApiErrorParser());
    }
    @After public void cleanup() throws Exception { server.shutdown(); }
    void body(String value, int code) { server.enqueue(new MockResponse().setResponseCode(code).setBody(value)); }
    void content() {
        body("{\"id\":\"" + ID + "\",\"word\":\"word\",\"cefr\":\"A1\",\"meaningVi\":\"từ\"}", 200);
        body("[]", 200);
    }
    LearningSessionRepository.Result load(boolean start) throws Exception {
        CountDownLatch done = new CountDownLatch(1); AtomicReference<LearningSessionRepository.Result> result = new AtomicReference<>();
        repository.load(owner, EVENT, ID, start ? null : ID, start, value -> { result.set(value); done.countDown(); });
        assertTrue(done.await(6, TimeUnit.SECONDS)); return result.get();
    }
    @Test public void startVerifiesUserUsesExactBodyThenLoadsSelectedCard() throws Exception {
        body(USER, 200); body(SESSION, 201); content(); var result = load(true); assertNotNull(result.card); assertEquals(ID, result.session.sessionId);
        assertEquals("/api/v1/users/me", server.takeRequest().getPath()); var start = server.takeRequest(); assertEquals("POST", start.getMethod());
        assertEquals("/api/v1/learning/sessions", start.getPath()); assertNull(start.getHeader("Idempotency-Key")); assertEquals("Bearer access-A", start.getHeader("Authorization"));
        JsonObject json = JsonParser.parseString(start.getBody().readUtf8()).getAsJsonObject(); assertEquals(2, json.size());
        assertEquals(EVENT, json.get("eventId").getAsString()); assertEquals("MIXED", json.get("sessionType").getAsString());
        assertEquals("/api/v1/vocabulary/" + ID, server.takeRequest().getPath()); assertEquals("/api/v1/vocabulary/" + ID + "/examples", server.takeRequest().getPath());
    }
    @Test public void resumeGetsExistingSessionWithoutMutation() throws Exception {
        body(USER, 200); body(SESSION, 200); content(); assertNotNull(load(false).card); server.takeRequest(); var get = server.takeRequest();
        assertEquals("GET", get.getMethod()); assertEquals("/api/v1/learning/sessions/" + ID, get.getPath()); assertEquals(4, server.getRequestCount());
    }
    @Test public void closedSessionDoesNotFetchContent() throws Exception {
        body(USER, 200); body(SESSION.replace("null", "\"2026-10-18T01:00:00Z\""), 200); var result = load(false);
        assertFalse(result.session.open()); assertNull(result.card); assertEquals(2, server.getRequestCount());
    }
    @Test public void rejectedIdentityNeverPosts() throws Exception {
        body(USER.replace("USER", "ADMIN"), 200); assertEquals(403, load(true).error.status); assertEquals(1, server.getRequestCount());
    }
    @Test public void reuseCodeIsPreservedAndNeverLoadsContent() throws Exception {
        body(USER, 200); body("{\"code\":\"IDEMPOTENCY_KEY_REUSE\"}", 409); assertTrue(load(true).error.isIdempotencyKeyReuse()); assertEquals(2, server.getRequestCount());
    }
    @Test public void invalidSessionNeverLeaksCardAndMissingGetKeeps404() throws Exception {
        body(USER, 200); body(SESSION.replace("MIXED", "REVIEW"), 201); assertNull(load(true).card);
        body(USER, 200); body("{\"code\":\"NOT_FOUND\"}", 404); assertEquals(404, load(false).error.status);
    }
    @Test public void contentFailureRetainsServerIdentityForGetRetry() throws Exception {
        body(USER, 200); body(SESSION, 201); body("{}", 503); var result = load(true); assertEquals(ID, result.session.sessionId); assertEquals(503, result.error.status);
    }
    @Test public void accountSwitchBetweenUserAndPostDropsOldOperation() throws Exception {
        CountDownLatch userReached = new CountDownLatch(1), allow = new CountDownLatch(1), callback = new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                userReached.countDown(); assertTrue(allow.await(5, TimeUnit.SECONDS)); return new MockResponse().setBody(USER);
            }
        });
        repository.load(owner, EVENT, ID, null, true, result -> callback.countDown()); assertTrue(userReached.await(5, TimeUnit.SECONDS));
        sessions.start("B", "refresh-B"); allow.countDown(); assertFalse(callback.await(400, TimeUnit.MILLISECONDS)); assertEquals(1, server.getRequestCount());
    }
}
