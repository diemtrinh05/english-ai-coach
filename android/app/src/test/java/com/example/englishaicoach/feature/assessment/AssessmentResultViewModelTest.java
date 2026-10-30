package com.example.englishaicoach.feature.assessment;

import static org.junit.Assert.*;
import androidx.arch.core.executor.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.ApiErrorParser;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.*;
import org.junit.*;
import java.math.BigDecimal;
import java.util.UUID;

public final class AssessmentResultViewModelTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    SessionManager sessions;
    AssessmentViewModelTest.Store store;
    AssessmentViewModelTest.Fake repository;
    OnboardingGateRepository.Callback gateCallback;
    AssessmentViewModel model;
    int gates;
    @Before public void setup() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            public void executeOnDiskIO(Runnable task) { task.run(); }
            public void postToMainThread(Runnable task) { task.run(); }
            public boolean isMainThread() { return true; }
        });
        sessions = new SessionManager(new TokenStore() {
            TokenSession token;
            public TokenSession read() { return token; }
            public void write(TokenSession value) { token = value; }
        });
        sessions.start("access", "refresh"); store = new AssessmentViewModelTest.Store(); repository = new AssessmentViewModelTest.Fake();
        store.value = new Assessment.Checkpoint(sessions.current().sessionId(), UUID.randomUUID().toString(), ID, null);
        model = create(); activate();
    }
    AssessmentViewModel create() {
        return new AssessmentViewModel(sessions, store, new AssessmentUseCase(repository),
                new ResolveOnboardingUseCase((owner, callback) -> { gates++; gateCallback = callback; return () -> { }; }), Runnable::run, () -> 0);
    }
    void activate() { model.setAuthenticated(true); model.setOnline(true); model.resume(); }
    void completed() { repository.result.completed(new Assessment.Result(ID, "COMPLETED", 28, 16, new BigDecimal("57.14"),
            new Assessment.Level("B1", "Trung cấp", "Mô tả server")), null); }
    @After public void close() { model.onCleared(); ArchTaskExecutor.getInstance().setDelegate(null); }
    @Test public void resultUsesStoredMetricsAndNoPost() {
        completed(); var result = model.state().getValue().result;
        assertEquals("B1", result.level.code); assertEquals(new BigDecimal("57.14"), result.score);
        assertEquals(16, result.correct); assertEquals(28, result.answered);
        assertEquals(0, repository.answers + repository.starts + gates);
    }
    @Test public void completedBookmarkSurvivesBackAndColdRecreationWithoutPost() {
        completed(); model.exit(); assertNotNull(store.value);
        model.onCleared(); model = create(); activate(); completed();
        assertEquals(2, repository.reads); assertEquals(0, repository.answers + repository.starts);
    }
    @Test public void onlyExplicitContinueResolvesGateAndClearsForEveryDestination() {
        for (OnboardingRoute route : OnboardingRoute.values()) {
            if (store.value == null) { model.onCleared(); store.value = new Assessment.Checkpoint(sessions.current().sessionId(), UUID.randomUUID().toString(), ID, null); model = create(); activate(); }
            completed(); int before = gates; assertNotNull(store.value);
            model.continueResult(); model.continueResult(); assertEquals(before + 1, gates); assertNotNull(store.value);
            gateCallback.completed(route, null); assertNull(store.value); assertEquals(route, model.state().getValue().route);
        }
    }
    @Test public void gateFailureKeepsBookmarkAndOnlyReadRetry() {
        completed(); model.continueResult(); gateCallback.completed(null, new ApiErrorParser().parse(503, null));
        assertNotNull(store.value); assertEquals(AssessmentViewModel.Phase.ERROR, model.state().getValue().phase);
        model.retry(); assertEquals(2, repository.reads); assertEquals(0, repository.answers + repository.starts);
    }
    @Test public void completedWithoutLevelCannotContinueOrUseDifficultyFallback() {
        repository.result.completed(new Assessment.Result(ID, "COMPLETED", 20), null);
        assertEquals(AssessmentViewModel.Phase.ERROR, model.state().getValue().phase);
        model.continueResult(); assertEquals(0, gates); assertNotNull(store.value);
    }
    @Test public void cancelledExitClearsWithoutCompletingOnboarding() {
        repository.result.completed(new Assessment.Result(ID, "CANCELLED", 4), null); model.continueResult();
        assertEquals(0, gates); model.exit(); assertNull(store.value);
    }
    @Test public void missingResultExitClearsAndRead503KeepsId() {
        repository.result.completed(null, new ApiErrorParser().parse(503, null)); assertNotNull(store.value);
        model.retry(); repository.result.completed(null, new ApiErrorParser().parse(404, null));
        model.exit(); assertNull(store.value); assertEquals(0, gates);
    }
    @Test public void accountSwitchDuringContinueCannotClearNewCheckpointOrRoute() {
        completed(); model.continueResult(); var old = gateCallback;
        sessions.start("B", "R"); Assessment.Checkpoint replacement = new Assessment.Checkpoint(sessions.current().sessionId(), UUID.randomUUID().toString(), ID, null);
        store.value = replacement; old.completed(OnboardingRoute.HOME, null);
        assertSame(replacement, store.value); assertEquals(AssessmentViewModel.Phase.EXPIRED, model.state().getValue().phase);
    }
    @Test public void checkpointClearFailureBlocksRouteAndKeepsEvidence() {
        completed(); model.continueResult(); store.fail = true; gateCallback.completed(OnboardingRoute.HOME, null);
        assertEquals(AssessmentViewModel.Phase.STORAGE, model.state().getValue().phase); assertNotNull(store.value);
    }
    @Test public void offlineContinueCannotResolveGateAndReconnectionOnlyReads() {
        completed(); model.setOnline(false); model.continueResult(); assertEquals(0, gates);
        model.setOnline(true); assertEquals(2, repository.reads); assertEquals(0, repository.answers + repository.starts + gates);
    }
    @Test public void resultNavigationObserverCannotRaceAccountSwitch() throws Exception {
        completed(); model.continueResult(); String owner = sessions.current().sessionId();
        java.util.concurrent.CountDownLatch attempted = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch switched = new java.util.concurrent.CountDownLatch(1);
        androidx.lifecycle.Observer<AssessmentViewModel.State> observer = value -> {
            if (value.phase != AssessmentViewModel.Phase.ROUTE) return;
            new Thread(() -> { attempted.countDown(); sessions.start("B", "R"); switched.countDown(); }).start();
            try {
                assertTrue(attempted.await(2, java.util.concurrent.TimeUnit.SECONDS));
                assertFalse(switched.await(100, java.util.concurrent.TimeUnit.MILLISECONDS));
                assertEquals(owner, sessions.current().sessionId());
            } catch (InterruptedException failure) { throw new AssertionError(failure); }
        };
        model.state().observeForever(observer);
        gateCallback.completed(OnboardingRoute.HOME, null);
        assertTrue(switched.await(2, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(AssessmentViewModel.Phase.EXPIRED, model.state().getValue().phase);
        model.state().removeObserver(observer);
    }
}
