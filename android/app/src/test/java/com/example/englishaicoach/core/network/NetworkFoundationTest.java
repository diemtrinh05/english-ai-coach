package com.example.englishaicoach.core.network;

import static org.junit.Assert.*;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.auth.TokenSession;
import com.example.englishaicoach.core.auth.TokenStore;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.SocketPolicy;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkFoundationTest {
    private MockWebServer server;
    private MemoryStore store;
    private SessionManager sessions;

    @Before public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        store = new MemoryStore();
        sessions = new SessionManager(store);
    }

    @After public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test public void errorParserKeepsCanonicalConflictsAndHttpStatus() {
        ApiErrorParser parser = new ApiErrorParser();
        ApiError concurrent = parser.parse(409, ResponseBody.create(
                okhttp3.MediaType.parse("application/json"),
                "{\"status\":409,\"code\":\"CONCURRENT_UPDATE\",\"message\":\"stale\"}"));
        assertEquals(ApiError.Kind.CONFLICT, concurrent.kind);
        assertTrue(concurrent.isConcurrentUpdate());
        assertEquals("CONCURRENT_UPDATE", concurrent.code);
        ApiError reuse = parser.parse(409, ResponseBody.create(
                okhttp3.MediaType.parse("application/json"),
                "{\"code\":\"IDEMPOTENCY_KEY_REUSE\"}"));
        assertTrue(reuse.isIdempotencyKeyReuse());
        assertEquals(409, reuse.status);
        assertEquals(ApiError.Kind.FORBIDDEN, parser.parse(403, null).kind);
        assertEquals(ApiError.Kind.UNAUTHORIZED, parser.parse(401, null).kind);
        assertEquals(ApiError.Kind.CONFLICT, parser.parse(409, ResponseBody.create(
                okhttp3.MediaType.parse("application/json"), "broken")).kind);
    }

    @Test public void logicalEventReusesIdOnlyForSameOperation() {
        LogicalEvent first = LogicalEvent.begin();
        assertSame(first, first.retry());
        assertEquals(first.eventId(), first.retry().eventId());
        assertNotEquals(first.eventId(), LogicalEvent.begin().eventId());
    }

    @Test public void authResponseRequiresBothTokensFromApprovedContract() {
        AuthResponse response = new Gson().fromJson("{\"accessToken\":\"a\",\"refreshToken\":\"r\","
                + "\"expiresIn\":1800,\"tokenType\":\"Bearer\",\"user\":{\"id\":\"u\"}}",
                AuthResponse.class);
        assertTrue(response.valid());
        response.refreshToken = null;
        assertFalse(response.valid());
    }

    @Test public void requestBoundToOldSessionCannotReplayAsNewAccount() throws Exception {
        sessions.start("access-a", "refresh-a");
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(sessions)).build();
        SessionBindingCallFactory calls = new SessionBindingCallFactory(client, sessions);
        okhttp3.Call oldCall = calls.newCall(new Request.Builder().url(server.url("/api/v1/users/me")).build());
        sessions.start("access-b", "refresh-b");
        try {
            oldCall.execute();
            fail("Old request must not execute under new session");
        } catch (IOException expected) {
            assertEquals(0, server.getRequestCount());
        }
    }

    @Test public void refreshRetriesOnceWithSameSessionAndNoIdempotencyHeader() throws Exception {
        sessions.start("old-access", "refresh-a");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(200).setBody(
                "{\"accessToken\":\"new-access\",\"refreshToken\":\"refresh-b\",\"expiresIn\":1800,\"tokenType\":\"Bearer\"}"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        OkHttpClient refreshClient = new OkHttpClient();
        RefreshService service = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .client(refreshClient).addConverterFactory(GsonConverterFactory.create())
                .build().create(RefreshService.class);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessions))
                .addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions))
                .authenticator(new SerializedRefreshAuthenticator(sessions, service)).build();
        SessionBindingCallFactory calls = new SessionBindingCallFactory(client, sessions);
        Request request = new Request.Builder().url(server.url("/api/v1/users/me")).build();
        try (Response response = calls.newCall(request).execute()) {
            assertEquals(200, response.code());
        }
        RecordedRequest initial = server.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest refresh = server.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest retry = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(initial);
        assertNotNull(refresh);
        assertNotNull(retry);
        assertEquals("Bearer old-access", initial.getHeader("Authorization"));
        assertEquals("Bearer new-access", retry.getHeader("Authorization"));
        assertEquals("/api/v1/auth/refresh", refresh.getPath());
        assertTrue(refresh.getBody().readUtf8().contains("refresh-a"));
        assertNull(retry.getHeader("Idempotency-Key"));
        assertEquals("new-access", sessions.current().accessToken());
        assertEquals("refresh-b", sessions.current().refreshToken());
    }

    @Test public void oldSessionRefreshFailureCannotClearNewSession() {
        sessions.start("a", "ra");
        TokenSession previous = sessions.current();
        sessions.start("b", "rb");
        sessions.clearIfCurrent(previous);
        assertEquals("b", sessions.current().accessToken());
        assertFalse(sessions.updateTokensIfCurrent(previous, "stale-refresh-result", "stale-refresh-token"));
        assertEquals("rb", sessions.current().refreshToken());
        assertEquals("b", sessions.current().accessToken());
    }

    @Test public void simultaneousUnauthorizedCallsReuseOneRefreshedToken() throws Exception {
        sessions.start("old-access", "refresh-a");
        TokenSession bound = sessions.current();
        server.enqueue(new MockResponse().setResponseCode(200).setBody(
                "{\"accessToken\":\"new-access\",\"refreshToken\":\"refresh-b\",\"expiresIn\":1800,\"tokenType\":\"Bearer\"}")
                .setBodyDelay(150, TimeUnit.MILLISECONDS));
        RefreshService service = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(RefreshService.class);
        SerializedRefreshAuthenticator authenticator = new SerializedRefreshAuthenticator(sessions, service);
        Request original = new Request.Builder().url(server.url("/api/v1/users/me"))
                .tag(TokenSession.class, bound).header("Authorization", "Bearer old-access").build();
        Response firstUnauthorized = new Response.Builder().request(original).protocol(okhttp3.Protocol.HTTP_1_1)
                .code(401).message("Unauthorized").build();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Request> first = pool.submit(() -> {
                start.await();
                return authenticator.authenticate(null, firstUnauthorized);
            });
            Future<Request> second = pool.submit(() -> {
                start.await();
                return authenticator.authenticate(null, firstUnauthorized);
            });
            start.countDown();
            Request firstRetry = first.get(3, TimeUnit.SECONDS);
            Request secondRetry = second.get(3, TimeUnit.SECONDS);
            assertNotNull(firstRetry);
            assertNotNull(secondRetry);
            assertEquals("Bearer new-access", firstRetry.header("Authorization"));
            assertEquals("Bearer new-access", secondRetry.header("Authorization"));
            assertEquals(1, server.getRequestCount());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test public void forbiddenResponseNeverTriggersRefresh() throws Exception {
        sessions.start("access", "refresh");
        server.enqueue(new MockResponse().setResponseCode(403).setBody("{}"));
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessions))
                .authenticator(new SerializedRefreshAuthenticator(sessions, request -> null)).build();
        SessionBindingCallFactory calls = new SessionBindingCallFactory(client, sessions);
        try (Response response = calls.newCall(new Request.Builder()
                .url(server.url("/api/v1/users/me")).build()).execute()) {
            assertEquals(403, response.code());
        }
        assertEquals(1, server.getRequestCount());
    }

    @Test public void refreshUnauthorizedClearsOnlyTheBoundSession() throws Exception {
        sessions.start("access-a", "refresh-a");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(401));
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertNull(sessions.current());
        assertEquals(2, server.getRequestCount());
        assertEquals("/api/v1/users/me", server.takeRequest().getPath());
        assertEquals("/api/v1/auth/refresh", server.takeRequest().getPath());
    }

    @Test public void refreshFailureFromOldAccountDoesNotClearNewAccount() throws Exception {
        sessions.start("access-a", "refresh-a");
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/api/v1/auth/refresh")) {
                    sessions.start("access-b", "refresh-b");
                }
                return new MockResponse().setResponseCode(401);
            }
        });
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertEquals("access-b", sessions.current().accessToken());
        assertEquals(2, server.getRequestCount());
    }

    @Test public void malformedSuccessResponseClearsSessionWithoutCrash() throws Exception {
        sessions.start("access", "refresh");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{ malformed json"));
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertNull(sessions.current());
        assertEquals(2, server.getRequestCount());
    }

    @Test public void invalidSuccessAndPermanentRefreshFailureClearSession() throws Exception {
        sessions.start("access", "refresh");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertNull(sessions.current());

        sessions.start("access-2", "refresh-2");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(400));
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertNull(sessions.current());
        assertEquals(4, server.getRequestCount());
    }

    @Test public void temporaryNetworkFailureKeepsSession() throws Exception {
        sessions.start("access", "refresh");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertEquals("access", sessions.current().accessToken());
        assertEquals(2, server.getRequestCount());
    }

    @Test public void temporaryServerFailureKeepsSession() throws Exception {
        sessions.start("access", "refresh");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(503));
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertEquals("access", sessions.current().accessToken());
        assertEquals(2, server.getRequestCount());
    }

    @Test public void secondUnauthorizedResponseDoesNotRefreshAgain() throws Exception {
        sessions.start("old-access", "refresh");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(200).setBody(
                "{\"accessToken\":\"new-access\",\"refreshToken\":\"refresh-b\",\"expiresIn\":1800,\"tokenType\":\"Bearer\"}"));
        server.enqueue(new MockResponse().setResponseCode(401));
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertEquals("new-access", sessions.current().accessToken());
        assertEquals(3, server.getRequestCount());
        assertEquals("/api/v1/users/me", server.takeRequest().getPath());
        assertEquals("/api/v1/auth/refresh", server.takeRequest().getPath());
        assertEquals("/api/v1/users/me", server.takeRequest().getPath());
    }

    @Test public void twoSuccessiveRefreshesSendRotatedRefreshToken() throws Exception {
        sessions.start("a0", "r0");
        for (int i = 1; i <= 2; i++) {
            server.enqueue(new MockResponse().setResponseCode(401));
            server.enqueue(new MockResponse().setResponseCode(200).setBody(
                    "{\"accessToken\":\"a" + i + "\",\"refreshToken\":\"r" + i
                            + "\",\"expiresIn\":900,\"tokenType\":\"Bearer\"}"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            try (Response response = boundClient().newCall(protectedRequest()).execute()) {
                assertEquals(200, response.code());
            }
            server.takeRequest();
            RecordedRequest refresh = server.takeRequest();
            assertTrue(refresh.getBody().readUtf8().contains("r" + (i - 1)));
            server.takeRequest();
            assertEquals("a" + i, sessions.current().accessToken());
            assertEquals("r" + i, sessions.current().refreshToken());
        }
    }

    @Test public void missingRotatedRefreshTokenClearsSessionWithoutPartialUpdate() throws Exception {
        sessions.start("a0", "r0");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(200).setBody(
                "{\"accessToken\":\"a1\",\"expiresIn\":900,\"tokenType\":\"Bearer\"}"));
        try (Response response = boundClient().newCall(protectedRequest()).execute()) {
            assertEquals(401, response.code());
        }
        assertNull(sessions.current());
        assertEquals(2, server.getRequestCount());
    }

    @Test public void accountSwitchImmediatelyAfterPairWriteCannotRebindRetry() throws Exception {
        sessions.start("access-a0", "refresh-a0");
        TokenSession boundA = sessions.current();
        server.enqueue(new MockResponse().setResponseCode(200).setBody(
                "{\"accessToken\":\"access-a1\",\"refreshToken\":\"refresh-a1\","
                        + "\"expiresIn\":900,\"tokenType\":\"Bearer\"}"));
        RefreshService service = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .client(ApiClient.refreshHttpClient()).addConverterFactory(GsonConverterFactory.create())
                .build().create(RefreshService.class);
        SerializedRefreshAuthenticator authenticator = new SerializedRefreshAuthenticator(sessions, service);
        Request original = protectedRequest().newBuilder().tag(TokenSession.class, boundA)
                .header("Authorization", "Bearer access-a0").build();
        Response unauthorized = new Response.Builder().request(original).protocol(okhttp3.Protocol.HTTP_1_1)
                .code(401).message("Unauthorized").build();
        // Chuyển tài khoản đúng lúc pair A đã ghi, trước khi authenticator tạo retry.
        store.afterNextWrite = () -> sessions.start("access-b", "refresh-b");
        Request retry = authenticator.authenticate(null, unauthorized);
        assertNotNull(retry);
        assertEquals("Bearer access-a1", retry.header("Authorization"));
        assertEquals(boundA.sessionId(), retry.tag(TokenSession.class).sessionId());
        assertEquals("refresh-a1", retry.tag(TokenSession.class).refreshToken());
        assertEquals("access-b", sessions.current().accessToken());
        assertEquals("refresh-b", sessions.current().refreshToken());
        assertNotEquals(sessions.current().sessionId(), retry.tag(TokenSession.class).sessionId());
        OkHttpClient guarded = new OkHttpClient.Builder().retryOnConnectionFailure(false)
                .addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions)).build();
        try {
            guarded.newCall(retry).execute();
            fail("Request phiên A phải bị chặn sau khi phiên B đã đăng nhập");
        } catch (IOException expected) {
            assertEquals("Session changed before network exchange", expected.getMessage());
        }
        assertEquals(1, server.getRequestCount());
        assertEquals("/api/v1/auth/refresh", server.takeRequest().getPath());
    }

    private SessionBindingCallFactory boundClient() {
        OkHttpClient refreshClient = ApiClient.refreshHttpClient();
        RefreshService service = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .client(refreshClient).addConverterFactory(GsonConverterFactory.create())
                .build().create(RefreshService.class);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessions))
                .addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions))
                .authenticator(new SerializedRefreshAuthenticator(sessions, service)).build();
        return new SessionBindingCallFactory(client, sessions);
    }

    private Request protectedRequest() {
        return new Request.Builder().url(server.url("/api/v1/users/me")).build();
    }

    private static final class MemoryStore implements TokenStore {
        private TokenSession value;
        private Runnable afterNextWrite;
        @Override public TokenSession read() { return value; }
        @Override public void write(TokenSession session) {
            value = session;
            Runnable hook = afterNextWrite;
            afterNextWrite = null;
            if (hook != null) hook.run();
        }
    }
}
