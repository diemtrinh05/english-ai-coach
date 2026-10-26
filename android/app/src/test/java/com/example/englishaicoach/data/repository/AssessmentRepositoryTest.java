package com.example.englishaicoach.data.repository;

import static org.junit.Assert.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.AssessmentApi;
import com.example.englishaicoach.domain.model.Assessment;
import com.example.englishaicoach.domain.repository.AssessmentRepository;
import com.google.gson.*;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.Set;

public final class AssessmentRepositoryTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    static final String EVENT = "00000000-0000-0000-0000-000000000002";
    static final String USER = "{\"id\":\"user\",\"email\":\"user@example.test\",\"fullName\":\"Tên\",\"role\":\"USER\"}";
    static final String ACK = "{\"correct\":false,\"isCompleted\":true,\"nextDifficulty\":\"C2\",\"questionsAnswered\":50}";
    static final String RESULT = "{\"assessmentId\":\"" + ID + "\",\"status\":\"IN_PROGRESS\",\"score\":null,\"totalQuestions\":0,\"correctAnswers\":0}";
    static final String QUESTION = "{\"questionId\":\"" + ID + "\",\"questionText\":\"Exact prompt\",\"options\":[\" A \",\"B\",\"C\",\"D\"],\"cefrLevel\":\"B2\"}";
    MockWebServer server; SessionManager sessions; AssessmentRepository repository; String owner;
    final AtomicReference<Object> result = new AtomicReference<>();
    final AtomicReference<ApiError> error = new AtomicReference<>();
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.start();
        sessions = new SessionManager(new TokenStore() {
            TokenSession value;
            public TokenSession read() { return value; }
            public void write(TokenSession value) { this.value = value; }
        });
        sessions.start("access-A", "refresh-A"); owner = sessions.current().sessionId();
        OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(false)
                .addInterceptor(new AuthInterceptor(sessions)).addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions)).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .callFactory(new SessionBindingCallFactory(client, sessions)).addConverterFactory(GsonConverterFactory.create()).build();
        repository = new RetrofitAssessmentRepository(sessions, retrofit.create(AssessmentApi.class), new ApiErrorParser());
    }
    @After public void cleanup() throws Exception { server.shutdown(); }
    void body(String body, int status) { server.enqueue(new MockResponse().setResponseCode(status).setBody(body)); }
    interface Invocation { void run(AssessmentRepository.Callback<Object> callback); }
    <T> void call(java.util.function.Consumer<AssessmentRepository.Callback<T>> action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        action.accept((value, failure) -> { result.set(value); error.set(failure); latch.countDown(); });
        assertTrue(latch.await(6, TimeUnit.SECONDS));
    }
    void answer() throws Exception { this.<Assessment.Ack>call(callback -> repository.answer(owner, ID, new Assessment.Answer(ID, " A ", 1234, EVENT), callback)); }
    @Test public void startPostsExactlyTypeAndEventAfterUserValidation() throws Exception {
        body(USER, 200); body("{\"assessmentId\":\"" + ID + "\",\"type\":\"INITIAL\",\"status\":\"IN_PROGRESS\",\"currentDifficulty\":\"A1\"}", 201);
        this.<String>call(callback -> repository.start(owner, new Assessment.Start(EVENT), callback));
        assertEquals(ID, result.get()); assertNull(error.get()); assertEquals("/api/v1/users/me", server.takeRequest().getPath());
        RecordedRequest post = server.takeRequest(); assertEquals("POST", post.getMethod()); assertEquals("/api/v1/assessments", post.getPath());
        JsonObject json = JsonParser.parseString(post.getBody().readUtf8()).getAsJsonObject();
        assertEquals(Set.of("type", "eventId"), json.keySet()); assertEquals("INITIAL", json.get("type").getAsString()); assertEquals(EVENT, json.get("eventId").getAsString());
    }
    @Test public void answerUsesRawFourFieldRequestAndNoClosedSessionGet() throws Exception {
        body(USER, 200); body(ACK, 200); answer(); assertNull(error.get());
        assertTrue(((Assessment.Ack) result.get()).completed); assertFalse(((Assessment.Ack) result.get()).correct);
        assertEquals("/api/v1/users/me", server.takeRequest().getPath()); RecordedRequest post = server.takeRequest();
        assertEquals("/api/v1/assessments/" + ID + "/answers", post.getPath()); assertEquals("POST", post.getMethod());
        assertEquals("Bearer access-A", post.getHeader("Authorization")); assertNull(post.getHeader("Idempotency-Key"));
        JsonObject json = JsonParser.parseString(post.getBody().readUtf8()).getAsJsonObject();
        assertEquals(Set.of("questionId", "answer", "responseTimeMs", "eventId"), json.keySet());
        assertEquals(" A ", json.get("answer").getAsString()); assertEquals(EVENT, json.get("eventId").getAsString()); assertEquals(2, server.getRequestCount());
    }
    @Test public void retryKeepsByteIdenticalBodyAndDoesNotBlindRetry503() throws Exception {
        body(USER, 200); body("{}", 503); answer(); assertEquals(503, error.get().status);
        assertEquals(2, server.getRequestCount()); server.takeRequest(); String first = server.takeRequest().getBody().readUtf8();
        body(USER, 200); body(ACK, 200); answer(); server.takeRequest(); assertEquals(first, server.takeRequest().getBody().readUtf8());
    }
    @Test public void questionOrderAndWhitespaceArePreservedWithoutCorrectAnswer() throws Exception {
        body(USER, 200); body(QUESTION, 200);
        this.<Assessment.Question>call(callback -> repository.question(owner, ID, callback));
        Assessment.Question value = (Assessment.Question) result.get(); assertEquals(" A ", value.options.get(0)); assertEquals("B2", value.level);
        assertEquals(4, value.options.size()); server.takeRequest(); RecordedRequest get = server.takeRequest();
        assertEquals("GET", get.getMethod()); assertEquals("/api/v1/assessments/" + ID + "/next-question", get.getPath());
    }
    @Test public void malformedQuestionOptionsAreRejectedInsteadOfInvented() throws Exception {
        for (String invalid : new String[]{QUESTION.replace("\"D\"", "\"B\""), QUESTION.replace(",\"D\"", ""), QUESTION.replace("\"B2\"", "\"Z9\"")}) {
            body(USER, 200); body(invalid, 200); this.<Assessment.Question>call(callback -> repository.question(owner, ID, callback));
            assertNull(result.get()); assertEquals(502, error.get().status);
        }
    }
    @Test public void resultAcceptsExplicitNullScoreAndUsesServerCount() throws Exception {
        body(USER, 200); body(RESULT, 200); this.<Assessment.Result>call(callback -> repository.result(owner, ID, callback));
        assertEquals(0, ((Assessment.Result) result.get()).answered); assertNull(error.get());
    }
    @Test public void resultRejectsMissingScoreWrongOwnerIdAndInvalidCounts() throws Exception {
        for (String invalid : new String[]{RESULT.replace("\"score\":null,", ""), RESULT.replace(ID, EVENT), RESULT.replace("\"correctAnswers\":0", "\"correctAnswers\":1")}) {
            body(USER, 200); body(invalid, 200); this.<Assessment.Result>call(callback -> repository.result(owner, ID, callback));
            assertNull(result.get()); assertEquals(502, error.get().status);
        }
    }
    @Test public void answerRejectsStringBooleanAndFractionalCounters() throws Exception {
        for (String invalid : new String[]{ACK.replace("\"correct\":false", "\"correct\":\"false\""), ACK.replace("50", "1.5")}) {
            body(USER, 200); body(invalid, 200); answer(); assertNull(result.get()); assertEquals(502, error.get().status);
        }
    }
    @Test public void canonicalErrorsRetainTheirExactIdentity() throws Exception {
        for (String code : new String[]{"ASSESSMENT_QUESTION_ALREADY_ANSWERED", "IDEMPOTENCY_KEY_REUSE", "ASSESSMENT_CONTENT_UNAVAILABLE"}) {
            body(USER, 200); body("{\"code\":\"" + code + "\"}", code.endsWith("UNAVAILABLE") ? 503 : 409); answer();
            assertNull(result.get()); assertEquals(code, error.get().code);
        }
    }
    @Test public void adminAndExpiredSessionCannotSubmit() throws Exception {
        body(USER.replace("USER", "ADMIN"), 200); answer(); assertEquals(403, error.get().status); assertEquals(1, server.getRequestCount());
        body("{}", 401); answer(); assertEquals(401, error.get().status); assertEquals(2, server.getRequestCount());
    }
    @Test public void accountSwitchBetweenUserValidationAndPostDropsCallbackAndRequest() throws Exception {
        CountDownLatch reached = new CountDownLatch(1), allow = new CountDownLatch(1), done = new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                reached.countDown(); assertTrue(allow.await(5, TimeUnit.SECONDS)); return new MockResponse().setBody(USER);
            }
        });
        repository.answer(owner, ID, new Assessment.Answer(ID, "B", 0, EVENT), (value, failure) -> done.countDown());
        assertTrue(reached.await(5, TimeUnit.SECONDS)); sessions.start("B", "R"); allow.countDown();
        assertFalse(done.await(400, TimeUnit.MILLISECONDS)); assertEquals(1, server.getRequestCount());
    }
}
