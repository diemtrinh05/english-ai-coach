package com.example.englishaicoach.data.repository;
import static org.junit.Assert.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.AuthFormsApi;
import com.example.englishaicoach.domain.repository.AuthFormsRepository;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import org.junit.*;
import java.util.concurrent.*;

public final class AuthFormsRepositoryTest {
    MockWebServer server; SessionManager sessions; RetrofitAuthFormsRepository repository;
    static final String BODY = "{\"accessToken\":\"access\",\"refreshToken\":\"refresh\",\"expiresIn\":1800,\"tokenType\":\"Bearer\",\"user\":{\"id\":\"id\",\"email\":\"a@example.com\",\"fullName\":\"Tên\",\"role\":\"USER\"}}";
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.start();
        sessions = new SessionManager(new TokenStore() {
            TokenSession value; public TokenSession read() { return value; } public void write(TokenSession s) { value = s; }
        });
        AuthFormsApi api = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .client(new OkHttpClient.Builder().retryOnConnectionFailure(false).followRedirects(false).build())
                .addConverterFactory(GsonConverterFactory.create()).build().create(AuthFormsApi.class);
        repository = new RetrofitAuthFormsRepository(sessions, api, new ApiErrorParser());
    }
    @After public void cleanup() throws Exception { server.shutdown(); }
    AuthFormsRepository.Result submit(boolean register) throws Exception {
        CountDownLatch done = new CountDownLatch(1); AuthFormsRepository.Result[] result = new AuthFormsRepository.Result[1];
        repository.submit(register, "Tên", "a@example.com", "password", value -> { result[0] = value; done.countDown(); });
        assertTrue(done.await(5, TimeUnit.SECONDS)); return result[0];
    }
    @Test public void loginPersistsAtomicPairAndExactRequest() throws Exception {
        server.enqueue(new MockResponse().setBody(BODY)); assertEquals(AuthFormsRepository.Result.SUCCESS, submit(false));
        assertEquals("access", sessions.current().accessToken()); assertEquals("refresh", sessions.current().refreshToken());
        RecordedRequest request = server.takeRequest(); assertEquals("/api/v1/auth/login", request.getPath());
        assertNull(request.getHeader("Authorization")); assertFalse(request.getBody().readUtf8().contains("fullName"));
    }
    @Test public void registerUses201AndNeverSendsConfirmation() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(201).setBody(BODY));
        assertEquals(AuthFormsRepository.Result.SUCCESS, submit(true)); String request = server.takeRequest().getBody().readUtf8();
        assertTrue(request.contains("fullName")); assertFalse(request.contains("confirm"));
    }
    @Test public void canonicalErrorsDoNotCreateSessionOrRetry() throws Exception {
        int[] status = {401,423,409,400,429,500}; String[] codes = {"AUTH_INVALID_CREDENTIALS","AUTH_ACCOUNT_LOCKED","CONFLICT","VALIDATION_ERROR","RATE_LIMIT_EXCEEDED","INTERNAL_ERROR"};
        AuthFormsRepository.Result[] expected = {AuthFormsRepository.Result.INVALID_CREDENTIALS,AuthFormsRepository.Result.LOCKED,
                AuthFormsRepository.Result.EMAIL_EXISTS,AuthFormsRepository.Result.VALIDATION,AuthFormsRepository.Result.RATE_LIMITED,AuthFormsRepository.Result.ERROR};
        for (int i=0;i<status.length;i++) {
            server.enqueue(new MockResponse().setResponseCode(status[i]).setBody("{\"code\":\""+codes[i]+"\"}"));
            assertEquals(expected[i], submit(false)); assertNull(sessions.current());
        }
        assertEquals(status.length, server.getRequestCount());
    }
    @Test public void malformedAndWrongStatusNeverPersist() throws Exception {
        String[] invalid = {BODY.replace("\"refresh\"", "null"), BODY.replace("\"access\"", "123"), BODY.replace("1800", "1.5"), BODY.replace("Bearer", "Other")};
        for (String body: invalid) { server.enqueue(new MockResponse().setBody(body)); assertNotEquals(AuthFormsRepository.Result.SUCCESS, submit(false)); assertNull(sessions.current()); }
        server.enqueue(new MockResponse().setBody(BODY)); assertEquals(AuthFormsRepository.Result.ERROR, submit(true)); assertNull(sessions.current());
    }
    @Test public void oldResponseCannotReplaceNewerSession() throws Exception {
        CountDownLatch received = new CountDownLatch(1), release = new CountDownLatch(1), done = new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException { received.countDown(); release.await(5,TimeUnit.SECONDS); return new MockResponse().setBody(BODY); }
        });
        AuthFormsRepository.Result[] result = new AuthFormsRepository.Result[1];
        repository.submit(false,"","a@example.com","password", value -> { result[0]=value; done.countDown(); });
        assertTrue(received.await(5,TimeUnit.SECONDS)); sessions.start("new-access","new-refresh"); release.countDown();
        assertTrue(done.await(5,TimeUnit.SECONDS)); assertEquals(AuthFormsRepository.Result.SUPERSEDED,result[0]); assertEquals("new-access",sessions.current().accessToken());
    }
    @Test public void cancelledSuccessCannotCreateSession() throws Exception {
        CountDownLatch received = new CountDownLatch(1), release = new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException { received.countDown(); release.await(5,TimeUnit.SECONDS); return new MockResponse().setBody(BODY); }
        });
        java.util.concurrent.atomic.AtomicInteger callbacks = new java.util.concurrent.atomic.AtomicInteger();
        var request = repository.submit(false,"","a@example.com","password", value -> callbacks.incrementAndGet());
        assertTrue(received.await(5,TimeUnit.SECONDS)); request.cancel(); release.countDown();
        assertNull(sessions.current()); assertEquals(0,callbacks.get());
    }
    @Test public void nullToSessionToNullInvalidatesOriginalOperation() {
        long expected = sessions.generation(); sessions.start("a","r"); sessions.clear();
        assertFalse(sessions.startIfUnchanged(expected,"late","late")); assertNull(sessions.current());
    }
}
