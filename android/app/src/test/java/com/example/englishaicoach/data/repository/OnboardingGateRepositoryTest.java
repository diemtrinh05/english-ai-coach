package com.example.englishaicoach.data.repository;

import static org.junit.Assert.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.OnboardingApi;
import com.example.englishaicoach.domain.model.OnboardingRoute;
import com.example.englishaicoach.domain.repository.OnboardingGateRepository;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class OnboardingGateRepositoryTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    static final String USER = "{\"id\":\"user\",\"email\":\"u@example.test\",\"fullName\":\"Tên\",\"role\":\"USER\"}";
    static final String LEVEL = "{\"id\":\"" + ID + "\",\"code\":\"B1\",\"name\":\"Trung cấp\",\"sortOrder\":3}";
    static final String PROFILE = "{\"fullName\":\"Tên\",\"timezone\":\"UTC\",\"dailyLearningMinutes\":20}";
    static final String GOALS = "{\"goals\":[{\"id\":\"" + ID + "\",\"name\":\"Goal đã inactive\",\"isPrimary\":false}]}";
    MockWebServer server;
    SessionManager sessions;
    OnboardingGateRepository repository;
    String owner;
    AtomicReference<OnboardingRoute> route = new AtomicReference<>();
    AtomicReference<ApiError> error = new AtomicReference<>();
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.start();
        sessions = new SessionManager(new TokenStore() {
            TokenSession token;
            public TokenSession read() { return token; }
            public void write(TokenSession value) { token = value; }
        });
        sessions.start("A", "R"); owner = sessions.current().sessionId();
        OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(false).addInterceptor(new AuthInterceptor(sessions))
                .addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions)).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .callFactory(new SessionBindingCallFactory(client, sessions)).addConverterFactory(GsonConverterFactory.create()).build();
        repository = new RetrofitOnboardingGateRepository(sessions, retrofit.create(OnboardingApi.class), new ApiErrorParser());
    }
    @After public void close() throws Exception { server.shutdown(); }
    void body(String data, int status) { server.enqueue(new MockResponse().setBody(data).setResponseCode(status)); }
    void resolve() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        repository.resolve(owner, (value, failure) -> { route.set(value); error.set(failure); latch.countDown(); });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    @Test public void missingProfileRoutesSettingsWithoutTreatingOther404AsSuccess() throws Exception {
        body(USER, 200); body("{}", 404); resolve(); assertEquals(OnboardingRoute.SETTINGS, route.get());
        assertEquals(2, server.getRequestCount()); assertNull(error.get());
    }
    @Test public void emptyGoalsRoutesSettingsEvenWhenProfileAlreadyHasCefr() throws Exception {
        body(USER, 200); body(PROFILE.replace("}", ",\"currentCefrLevel\":" + LEVEL + "}"), 200); body("{\"goals\":[]}", 200);
        resolve(); assertEquals(OnboardingRoute.SETTINGS, route.get());
    }
    @Test public void savedGoalsWithoutProfileCefrRequiresPlacement() throws Exception {
        body(USER, 200); body(PROFILE, 200); body(GOALS, 200); resolve(); assertEquals(OnboardingRoute.PLACEMENT, route.get());
    }
    @Test public void savedInactiveGoalCountsAndFullServerPredicateRoutesHomeUsingOnlyGets() throws Exception {
        body(USER, 200); body(PROFILE.replace("}", ",\"currentCefrLevel\":" + LEVEL + "}"), 200); body(GOALS, 200);
        resolve(); assertEquals(OnboardingRoute.HOME, route.get());
        for (String path : new String[]{"users/me", "users/me/profile", "users/me/goals"}) {
            RecordedRequest request = server.takeRequest(); assertEquals("GET", request.getMethod());
            assertEquals("/api/v1/" + path, request.getPath()); assertEquals("Bearer A", request.getHeader("Authorization"));
        }
        assertEquals(3, server.getRequestCount());
    }
    @Test public void explicitNullProfileLevelDoesNotBecomeCompletionHint() throws Exception {
        body(USER, 200); body(PROFILE.replace("}", ",\"currentCefrLevel\":null}"), 200); body(GOALS, 200);
        resolve(); assertEquals(OnboardingRoute.PLACEMENT, route.get());
    }
    @Test public void profile503AndGoal404DoNotRouteOrGuessEmptyGoals() throws Exception {
        body(USER, 200); body("{}", 503); resolve(); assertNull(route.get()); assertEquals(503, error.get().status);
        body(USER, 200); body(PROFILE, 200); body("{}", 404); resolve(); assertNull(route.get()); assertEquals(404, error.get().status);
    }
    @Test public void malformedCefrAndProfileCannotSilentlyRouteHome() throws Exception {
        for (String profile : new String[]{PROFILE.replace("}", ",\"currentCefrLevel\":{\"code\":\"B1\"}}"), PROFILE.replace("20", "1.5")}) {
            body(USER, 200); body(profile, 200); resolve(); assertNull(route.get()); assertEquals(502, error.get().status);
        }
    }
    @Test public void malformedGoalsCannotBeTreatedAsNoSelection() throws Exception {
        body(USER, 200); body(PROFILE, 200); body("{\"goals\":null}", 200); resolve();
        assertNull(route.get()); assertEquals(502, error.get().status);
    }
    @Test public void adminAndExpiredSessionStopBeforePrivateReads() throws Exception {
        body(USER.replace("USER", "ADMIN"), 200); resolve(); assertEquals(403, error.get().status);
        body("{}", 401); resolve(); assertEquals(401, error.get().status); assertEquals(2, server.getRequestCount());
    }
    @Test public void accountSwitchBetweenReadStagesCannotRequestOrPublishForNewOwner() throws Exception {
        CountDownLatch arrived = new CountDownLatch(1), release = new CountDownLatch(1), callback = new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                arrived.countDown(); assertTrue(release.await(5, TimeUnit.SECONDS)); return new MockResponse().setBody(USER);
            }
        });
        repository.resolve(owner, (value, failure) -> callback.countDown()); assertTrue(arrived.await(5, TimeUnit.SECONDS));
        sessions.start("B", "R"); release.countDown(); assertFalse(callback.await(350, TimeUnit.MILLISECONDS));
        assertEquals(1, server.getRequestCount());
    }
}
