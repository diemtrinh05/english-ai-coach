package com.example.englishaicoach.data.repository;

import static org.junit.Assert.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.model.LearningAttempt;
import com.google.gson.*;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class LearningAttemptRepositoryTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    static final String EVENT = "00000000-0000-0000-0000-000000000002";
    static final String USER = "{\"id\":\"user\",\"email\":\"user@example.test\",\"fullName\":\"Tên\",\"role\":\"USER\"}";
    static final String SESSION = "{\"sessionId\":\"" + ID + "\",\"sessionType\":\"MIXED\",\"startedAt\":\"2026-10-18T00:00:00Z\",\"endedAt\":null}";
    MockWebServer server; SessionManager sessions; RetrofitLearningAttemptRepository repository; String owner;
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.start(); sessions = new SessionManager(new TokenStore() {
            TokenSession value; public TokenSession read() { return value; } public void write(TokenSession token) { value = token; }
        }); sessions.start("access-A", "refresh-A"); owner = sessions.current().sessionId();
        OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(false)
                .addInterceptor(new AuthInterceptor(sessions)).addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions)).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .callFactory(new SessionBindingCallFactory(client, sessions)).addConverterFactory(GsonConverterFactory.create()).build();
        repository = new RetrofitLearningAttemptRepository(sessions, retrofit.create(LearningAttemptApi.class), new ApiErrorParser());
    }
    @After public void cleanup() throws Exception { server.shutdown(); }
    void body(String value, int code) { server.enqueue(new MockResponse().setResponseCode(code).setBody(value)); }
    static final String PROGRESS="{\"vocabularyId\":\""+ID+"\",\"status\":\"REVIEWING\",\"easeFactor\":2.5,\"intervalDays\":17,\"repetitions\":3,\"nextReviewAt\":\"2026-11-04T00:00:00Z\",\"correctCount\":5,\"incorrectCount\":1}";
    static final String RESULT="{\"attemptId\":\""+EVENT+"\",\"isCorrect\":false,\"answerQuality\":4,\"srs\":{\"oldEaseFactor\":2.5,\"newEaseFactor\":2.5,\"oldIntervalDays\":6,\"newIntervalDays\":17,\"oldRepetitions\":2,\"newRepetitions\":3,\"nextReviewAt\":\"2026-11-04T00:00:00Z\",\"algorithmVersion\":\"sm2-ext-v1\"},\"progress\":"+PROGRESS+"}";
    final AtomicReference<LearningAttempt> result=new AtomicReference<>();
    final AtomicReference<ApiError> error=new AtomicReference<>();
    LearningAttempt.Request request() { return new LearningAttempt.Request(ID,ID,2500,4,EVENT); }
    void submit() throws Exception {
        CountDownLatch done=new CountDownLatch(1);
        repository.submit(owner,request(),(value,failure)->{result.set(value);error.set(failure);done.countDown();});
        assertTrue(done.await(6,TimeUnit.SECONDS));
    }
    boolean refresh() throws Exception {
        CountDownLatch done=new CountDownLatch(1); AtomicBoolean open=new AtomicBoolean();
        repository.refresh(owner,ID,ID,(value,failure)->{open.set(value);error.set(failure);done.countDown();});
        assertTrue(done.await(6,TimeUnit.SECONDS));return open.get();
    }
    @Test public void exactSixFieldRequestHasNoCorrectnessOrVersionAndUsesSharedBearer() throws Exception {
        body(USER,200);body(RESULT,200);submit();assertNull(error.get());assertFalse(result.get().isCorrect);
        assertEquals(17,result.get().intervalDays);assertEquals("/api/v1/users/me",server.takeRequest().getPath());
        var post=server.takeRequest();assertEquals("/api/v1/learning/attempts",post.getPath());assertEquals("POST",post.getMethod());
        assertEquals("Bearer access-A",post.getHeader("Authorization"));assertNull(post.getHeader("Idempotency-Key"));
        var json=JsonParser.parseString(post.getBody().readUtf8()).getAsJsonObject();
        assertEquals(java.util.Set.of("sessionId","vocabularyId","attemptType","responseTimeMs","answerQuality","eventId"),json.keySet());
        assertEquals("FLASHCARD",json.get("attemptType").getAsString());assertEquals(2500,json.get("responseTimeMs").getAsInt());
        assertEquals(4,json.get("answerQuality").getAsInt());assertEquals(EVENT,json.get("eventId").getAsString());
        assertEquals(2,server.getRequestCount());
    }
    @Test public void explicitRetryAfterAmbiguousFailureKeepsByteIdenticalBodyAndNoSessionGet() throws Exception {
        body(USER,200);body("{}",503);submit();assertEquals(503,error.get().status);server.takeRequest();String first=server.takeRequest().getBody().readUtf8();
        body(USER,200);body(RESULT,200);submit();server.takeRequest();assertEquals(first,server.takeRequest().getBody().readUtf8());assertNull(error.get());
        assertEquals(4,server.getRequestCount());
    }
    @Test public void both409CodesRemainDistinctWithoutMutationRetry() throws Exception {
        body(USER,200);body("{\"code\":\"CONCURRENT_UPDATE\"}",409);submit();assertTrue(error.get().isConcurrentUpdate());
        body(USER,200);body("{\"code\":\"IDEMPOTENCY_KEY_REUSE\"}",409);submit();assertTrue(error.get().isIdempotencyKeyReuse());assertEquals(4,server.getRequestCount());
    }
    @Test public void conflictRefreshRequiresProgressAndSessionReadsWithoutPost() throws Exception {
        body(USER,200);body(PROGRESS,200);body(SESSION,200);assertTrue(refresh());assertNull(error.get());
        assertEquals("/api/v1/users/me",server.takeRequest().getPath());assertEquals("/api/v1/vocabulary/"+ID+"/progress",server.takeRequest().getPath());
        var last=server.takeRequest();assertEquals("GET",last.getMethod());assertEquals("/api/v1/learning/sessions/"+ID,last.getPath());
    }
    @Test public void failedProgressRefreshNeverClaimsReadyOrFetchesSession() throws Exception {
        body(USER,200);body("{}",503);assertFalse(refresh());assertEquals(503,error.get().status);assertEquals(2,server.getRequestCount());
    }
    @Test public void closedSessionIsReportedAfterBothReads() throws Exception {
        body(USER,200);body(PROGRESS,200);body(SESSION.replace("null","\"2026-10-18T01:00:00Z\""),200);assertFalse(refresh());assertNull(error.get());
    }
    @Test public void malformedResponseDoesNotProduceFakeSuccessAndRemainsReplayable() throws Exception {
        body(USER,200);body(RESULT.replace("\"isCorrect\":false","\"isCorrect\":\"false\""),200);submit();assertNull(result.get());assertEquals(502,error.get().status);
        body(USER,200);body(RESULT.replace("\"answerQuality\":4","\"answerQuality\":3"),200);submit();assertNull(result.get());assertEquals(502,error.get().status);
    }
    @Test public void invalidProgressOrNumericStringsRejectResponse() throws Exception {
        body(USER,200);body(RESULT.replace("\"intervalDays\":17","\"intervalDays\":\"17\""),200);submit();assertEquals(502,error.get().status);
        body(USER,200);body(PROGRESS.replace("\"REVIEWING\"","\"UNKNOWN\""),200);assertFalse(refresh());assertEquals(502,error.get().status);
    }
    @Test public void nonUserNeverPostsAndUnknownOwnerMakesNoCall() throws Exception {
        body(USER.replace("USER","ADMIN"),200);submit();assertEquals(403,error.get().status);assertEquals(1,server.getRequestCount());
        sessions.start("B","r");repository.submit(owner,request(),(v,e)->fail("Không callback phiên cũ"));assertEquals(1,server.getRequestCount());
    }
    @Test public void switchBetweenAuthAndPostDropsOldOwner() throws Exception {
        CountDownLatch authReached=new CountDownLatch(1),allow=new CountDownLatch(1),done=new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher(){public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            authReached.countDown();assertTrue(allow.await(5,TimeUnit.SECONDS));return new MockResponse().setBody(USER);
        }});
        repository.submit(owner,request(),(v,e)->done.countDown());assertTrue(authReached.await(5,TimeUnit.SECONDS));sessions.start("B","r");allow.countDown();
        assertFalse(done.await(400,TimeUnit.MILLISECONDS));assertEquals(1,server.getRequestCount());
    }
}
