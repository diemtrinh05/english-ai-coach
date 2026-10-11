package com.example.englishaicoach.data.repository;

import static org.junit.Assert.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.CurrentUserApi;
import com.example.englishaicoach.domain.repository.AuthSessionRepository;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class AuthSessionRepositoryTest {
    private MockWebServer server;
    private SessionManager sessions;
    private RetrofitAuthSessionRepository repository;
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.start();
        sessions = new SessionManager(new TokenStore() {
            TokenSession value;
            public TokenSession read() { return value; }
            public void write(TokenSession session) { value = session; }
        });
        Retrofit refresh = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .addConverterFactory(GsonConverterFactory.create()).build();
        OkHttpClient http = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(sessions))
                .addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions))
                .authenticator(new SerializedRefreshAuthenticator(sessions, refresh.create(RefreshService.class))).build();
        CurrentUserApi api = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .callFactory(new SessionBindingCallFactory(http, sessions))
                .addConverterFactory(GsonConverterFactory.create()).build().create(CurrentUserApi.class);
        repository = new RetrofitAuthSessionRepository(sessions, api);
    }
    @After public void teardown() throws Exception { server.shutdown(); }
    private AuthSessionRepository.Result restore() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<AuthSessionRepository.Result> result = new AtomicReference<>();
        repository.restore(value -> { result.set(value); done.countDown(); });
        assertTrue(done.await(6, TimeUnit.SECONDS));
        return result.get();
    }
    private MockResponse user() {
        return new MockResponse().setBody("{\"id\":\"00000000-0000-0000-0000-000000000001\",\"email\":\"a@example.test\",\"fullName\":\"Người học\",\"role\":\"USER\"}");
    }
    @Test public void missingStoredSessionRoutesLoginWithoutNetwork() throws Exception {
        assertEquals(AuthSessionRepository.Result.UNAUTHENTICATED, restore());
        assertEquals(0, server.getRequestCount());
    }
    @Test public void storedSessionProbesCanonicalCurrentUser() throws Exception {
        sessions.start("access", "refresh"); server.enqueue(user());
        assertEquals(AuthSessionRepository.Result.AUTHENTICATED, restore());
        RecordedRequest request = server.takeRequest();
        assertEquals("/api/v1/users/me", request.getPath());
        assertEquals("Bearer access", request.getHeader("Authorization"));
    }
    @Test public void expiredAccessRotatesPairAndRestoresOnce() throws Exception {
        sessions.start("old", "refresh"); server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setBody("{\"accessToken\":\"new\",\"refreshToken\":\"rotated\",\"expiresIn\":1800,\"tokenType\":\"Bearer\"}"));
        server.enqueue(user());
        assertEquals(AuthSessionRepository.Result.AUTHENTICATED, restore());
        assertEquals("new", sessions.current().accessToken());
        assertEquals("rotated", sessions.current().refreshToken());
        assertEquals(3, server.getRequestCount());
    }
    @Test public void revokedRefreshClearsOnlyCurrentSession() throws Exception {
        sessions.start("old", "revoked"); server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"code\":\"AUTH_REFRESH_TOKEN_INVALID\"}"));
        assertEquals(AuthSessionRepository.Result.SESSION_EXPIRED, restore()); assertNull(sessions.current());
    }
    @Test public void transientRefreshKeepsPairForRetry() throws Exception {
        sessions.start("old", "refresh"); TokenSession before = sessions.current();
        server.enqueue(new MockResponse().setResponseCode(401)); server.enqueue(new MockResponse().setResponseCode(503));
        assertEquals(AuthSessionRepository.Result.ERROR, restore()); assertEquals(before, sessions.current());
    }
    @Test public void retry401StopsAndExpiresRotatedSnapshot() throws Exception {
        sessions.start("old", "refresh"); server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setBody("{\"accessToken\":\"new\",\"refreshToken\":\"rotated\",\"expiresIn\":1800,\"tokenType\":\"Bearer\"}"));
        server.enqueue(new MockResponse().setResponseCode(401));
        assertEquals(AuthSessionRepository.Result.SESSION_EXPIRED, restore());
        assertNull(sessions.current()); assertEquals(3, server.getRequestCount());
    }
    @Test public void forbiddenDoesNotRefreshOrEraseCredentials() throws Exception {
        sessions.start("access", "refresh"); server.enqueue(new MockResponse().setResponseCode(403));
        assertEquals(AuthSessionRepository.Result.ERROR, restore()); assertNotNull(sessions.current());
        assertEquals(1, server.getRequestCount());
    }
    @Test public void malformed200CannotAuthenticateAndKeepsCredential() throws Exception {
        sessions.start("access", "refresh"); server.enqueue(new MockResponse().setBody("{}"));
        assertEquals(AuthSessionRepository.Result.ERROR, restore()); assertNotNull(sessions.current());
    }
    @Test public void lateAccountAResponseCannotAuthenticateAccountB() throws Exception {
        sessions.start("a", "r");
        CountDownLatch received = new CountDownLatch(1), release = new CountDownLatch(1), callback = new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                received.countDown(); release.await(5, TimeUnit.SECONDS); return user();
            }
        });
        repository.restore(value -> callback.countDown());
        assertTrue(received.await(5, TimeUnit.SECONDS)); sessions.start("b", "s"); release.countDown();
        assertFalse(callback.await(400, TimeUnit.MILLISECONDS));
        assertEquals("b", sessions.current().accessToken());
    }
}
