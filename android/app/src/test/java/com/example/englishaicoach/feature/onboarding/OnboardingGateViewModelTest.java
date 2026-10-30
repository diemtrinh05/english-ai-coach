package com.example.englishaicoach.feature.onboarding;

import static org.junit.Assert.*;
import androidx.arch.core.executor.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.ApiErrorParser;
import com.example.englishaicoach.domain.model.OnboardingRoute;
import com.example.englishaicoach.domain.repository.OnboardingGateRepository;
import com.example.englishaicoach.domain.usecase.ResolveOnboardingUseCase;
import org.junit.*;

public final class OnboardingGateViewModelTest {
    SessionManager sessions;
    Fake repository;
    OnboardingGateViewModel model;
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
        sessions.start("access", "refresh"); repository = new Fake();
        model = new OnboardingGateViewModel(sessions, new ResolveOnboardingUseCase(repository), Runnable::run);
    }
    void activate() { model.setAuthenticated(true); model.setOnline(true); model.resume(); }
    @After public void close() { model.onCleared(); ArchTaskExecutor.getInstance().setDelegate(null); }
    @Test public void offlineAndUnauthenticatedNeverReadOrAssumeCompletion() {
        model.resume(); model.retry(); assertEquals(0, repository.reads);
        model.setOnline(false); assertEquals(OnboardingGateViewModel.Phase.OFFLINE, model.state().getValue().phase);
    }
    @Test public void duplicateReadBlockedAndAllThreeServerRoutesPreserved() {
        activate(); model.load(); assertEquals(1, repository.reads);
        for (OnboardingRoute route : OnboardingRoute.values()) {
            repository.callback.completed(route, null);
            assertEquals(route, model.state().getValue().route);
            model.retry();
        }
    }
    @Test public void readFailureRequiresReadRetryWithoutPublishingHome() {
        activate(); repository.callback.completed(null, new ApiErrorParser().parse(503, null));
        assertEquals(OnboardingGateViewModel.Phase.ERROR, model.state().getValue().phase);
        assertNull(model.state().getValue().route); model.retry(); assertEquals(2, repository.reads);
    }
    @Test public void pauseInvalidatesLateSuccessAndResumeRevalidates() {
        activate(); var old = repository.callback; model.pause(); old.completed(OnboardingRoute.HOME, null);
        assertNull(model.state().getValue().route); model.resume(); assertEquals(2, repository.reads);
    }
    @Test public void accountSwitchRejectsOldRouteAndNewRequests() {
        activate(); var old = repository.callback; sessions.start("other", "refresh");
        old.completed(OnboardingRoute.HOME, null); model.retry();
        assertEquals(OnboardingGateViewModel.Phase.EXPIRED, model.state().getValue().phase); assertEquals(1, repository.reads);
    }
    @Test public void completedNavigationIsConsumedWithoutSecondRouting() {
        activate(); repository.callback.completed(OnboardingRoute.HOME, null); model.routed();
        model.pause(); model.resume(); model.setOnline(true); assertNull(model.state().getValue().route);
        assertEquals(1, repository.reads);
    }
    @Test public void refreshKeepsStableOwnerAndAllowsReadResult() {
        activate(); String owner = repository.owner;
        sessions.updateTokensIfCurrent(sessions.current(), "rotated", "rotated-refresh");
        repository.callback.completed(OnboardingRoute.PLACEMENT, null);
        assertEquals(owner, sessions.current().sessionId()); assertEquals(OnboardingRoute.PLACEMENT, model.state().getValue().route);
    }
    @Test public void routeObserverAndAccountSwitchAreSerialized() throws Exception {
        activate(); String owner = sessions.current().sessionId();
        java.util.concurrent.CountDownLatch attempted = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch switched = new java.util.concurrent.CountDownLatch(1);
        androidx.lifecycle.Observer<OnboardingGateViewModel.State> observer = value -> {
            if (value.phase != OnboardingGateViewModel.Phase.READY) return;
            new Thread(() -> { attempted.countDown(); sessions.start("B", "R"); switched.countDown(); }).start();
            try {
                assertTrue(attempted.await(2, java.util.concurrent.TimeUnit.SECONDS));
                assertFalse(switched.await(100, java.util.concurrent.TimeUnit.MILLISECONDS));
                assertEquals(owner, sessions.current().sessionId());
            } catch (InterruptedException failure) { throw new AssertionError(failure); }
        };
        model.state().observeForever(observer);
        repository.callback.completed(OnboardingRoute.HOME, null);
        assertTrue(switched.await(2, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(OnboardingGateViewModel.Phase.EXPIRED, model.state().getValue().phase);
        model.state().removeObserver(observer);
    }
    static final class Fake implements OnboardingGateRepository {
        Callback callback; int reads; String owner;
        public Cancelable resolve(String owner, Callback callback) { reads++; this.owner = owner; this.callback = callback; return () -> { }; }
    }
}
