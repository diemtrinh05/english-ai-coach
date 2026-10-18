package com.example.englishaicoach.feature.learning;

import static org.junit.Assert.*;
import androidx.arch.core.executor.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.LearningSessionRepository;
import com.example.englishaicoach.domain.usecase.OpenLearningSessionUseCase;
import org.junit.*;
import java.util.*;

public final class LearningSessionViewModelTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    SessionManager sessions; Fake repository; LearningSessionViewModel model; long now;
    @Before public void setup() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            public void executeOnDiskIO(Runnable task) { task.run(); }
            public void postToMainThread(Runnable task) { task.run(); }
            public boolean isMainThread() { return true; }
        });
        sessions = new SessionManager(new TokenStore() {
            TokenSession value; public TokenSession read() { return value; } public void write(TokenSession token) { value = token; }
        }); sessions.start("access", "refresh"); repository = new Fake(); model = create(null);
    }
    LearningSessionViewModel create(LearningSessionViewModel.Snapshot saved) {
        return new LearningSessionViewModel(sessions, new OpenLearningSessionUseCase(repository), Runnable::run, () -> now, ID, saved);
    }
    void activate() { model.setAuthenticated(true); model.setOnline(true); model.resume(); }
    void loaded() { activate(); repository.latest().completed(success()); }
    LearningSessionRepository.Result success() {
        return new LearningSessionRepository.Result(new LearningSession(ID, "MIXED", "2026-10-18T00:00:00Z", null),
                new VocabularyItem(ID, "word", "/w/", "từ", "word", "NOUN", "A1", List.of(), null, List.of()), null);
    }
    @After public void cleanup() { model.onCleared(); ArchTaskExecutor.getInstance().setDelegate(null); }
    @Test public void startOnceAndDuplicateResumeDoesNotDuplicatePendingMutation() {
        activate(); model.resume(); model.retry(); assertEquals(1, repository.calls.size()); assertTrue(repository.starts.get(0));
    }
    @Test public void timeoutExplicitRetryKeepsEventAndPayload() {
        activate(); String event = repository.events.get(0);
        repository.latest().completed(new LearningSessionRepository.Result(null, null, new ApiErrorParser().parse(0, null)));
        model.retry(); assertEquals(2, repository.calls.size()); assertEquals(event, repository.events.get(1)); assertEquals(ID, repository.vocabulary);
    }
    @Test public void offlineNeverStartsOrAutomaticallyRetriesPending() {
        model.setAuthenticated(true); model.resume(); assertTrue(repository.calls.isEmpty());
        model.setOnline(true); assertEquals(1, repository.calls.size()); model.setOnline(false); model.setOnline(true);
        assertEquals(1, repository.calls.size()); model.retry(); assertEquals(2, repository.calls.size());
    }
    @Test public void initialOfflineEntryRequiresExplicitStartAfterConnectionReturns() {
        model.setOnline(false); model.setAuthenticated(true); model.resume(); model.setOnline(true);
        assertTrue(repository.calls.isEmpty()); model.retry(); assertEquals(1, repository.calls.size());
    }
    @Test public void timerRevealAndBackDoNotReset() {
        loaded(); now = 1000; model.reveal(); now = 1500; assertEquals(1500, model.elapsedMs());
        assertTrue(model.backToFront()); assertEquals(1500, model.elapsedMs()); assertFalse(model.backToFront());
    }
    @Test public void backgroundAndRevalidationDoNotInflateTimer() {
        loaded(); now = 900; model.pause(); now = 9900; model.resume(); assertEquals(900, model.elapsedMs());
        assertFalse(repository.starts.get(1)); repository.latest().completed(success()); now = 10100; assertEquals(1100, model.elapsedMs());
    }
    @Test public void offlinePausesAndRevalidatesBeforeResumingTimer() {
        loaded(); now = 700; model.setOnline(false); now = 9000; model.setOnline(true); assertEquals(700, model.elapsedMs());
        repository.latest().completed(success()); now = 9100; assertEquals(800, model.elapsedMs());
    }
    @Test public void rotationRetainsFaceElapsedAndSessionWithGetOnly() {
        loaded(); model.reveal(); now = 500; model.pause(); now = 5000; model.resume(); repository.latest().completed(success());
        assertEquals(LearningSessionViewModel.Phase.BACK, model.state().getValue().phase);
        assertEquals(500, model.elapsedMs()); assertFalse(repository.starts.get(1)); assertEquals(ID, model.snapshot().server);
    }
    @Test public void processRecreationPendingRequiresExplicitSameEventRetry() {
        activate(); var saved = model.snapshot(); model.onCleared(); model = create(saved); activate(); assertEquals(1, repository.calls.size());
        model.retry(); assertEquals(saved.event, repository.events.get(1)); assertTrue(repository.starts.get(1));
    }
    @Test public void processRecreationOpenSessionGetAndResetsFaceAndTimer() {
        loaded(); model.reveal(); now = 800; var saved = model.snapshot(); model.onCleared(); model = create(saved); activate();
        assertFalse(repository.starts.get(1)); repository.latest().completed(success());
        assertEquals(LearningSessionViewModel.Phase.FRONT, model.state().getValue().phase); assertEquals(0, model.elapsedMs());
    }
    @Test public void restoreWaitsForAuthenticatedGate() {
        loaded(); var saved = model.snapshot(); model.onCleared(); model = create(saved); model.setOnline(true); model.resume();
        assertEquals(1, repository.calls.size()); model.setAuthenticated(true); assertEquals(2, repository.calls.size());
    }
    @Test public void changedOwnerClearsSnapshotAndDropsLateResponse() {
        activate(); var callback = repository.latest(); sessions.start("new", "new-refresh"); callback.completed(success());
        assertEquals(LearningSessionViewModel.Phase.EXPIRED, model.state().getValue().phase); assertNull(model.snapshot().event); assertNull(model.state().getValue().card);
    }
    @Test public void restoredOtherOwnerNeverMakesNetworkRequest() {
        var saved = model.snapshot(); model.onCleared(); sessions.start("new", "r"); model = create(saved); activate(); assertTrue(repository.calls.isEmpty());
        assertEquals(LearningSessionViewModel.Phase.EXPIRED, model.state().getValue().phase);
    }
    @Test public void refreshKeepsOwnerAndSameEvent() {
        activate(); String event = model.snapshot().event; TokenSession old = sessions.current(); sessions.updateTokensIfCurrent(old, "rotated", "rotated-r");
        repository.latest().completed(new LearningSessionRepository.Result(null, null, new ApiErrorParser().parse(503, null))); model.retry(); assertEquals(event, repository.events.get(1));
    }
    @Test public void closedSessionNeverLoadsOrStartsAgain() {
        activate(); repository.latest().completed(new LearningSessionRepository.Result(new LearningSession(ID, "MIXED", "now", "ended"), null, null));
        model.retry(); model.pause(); model.resume(); assertEquals(1, repository.calls.size()); assertEquals(LearningSessionViewModel.Phase.CLOSED, model.state().getValue().phase);
    }
    @Test public void startSuccessThenContentFailureRetriesGetInsteadOfPost() {
        activate(); repository.latest().completed(new LearningSessionRepository.Result(success().session, null, new ApiErrorParser().parse(503, null)));
        model.retry(); assertFalse(repository.starts.get(1)); assertEquals(ID, model.snapshot().server);
    }
    @Test public void exitClearsPrivateStateAndCancelsLateResponse() {
        activate(); var late = repository.latest(); model.exit(); late.completed(success()); assertNull(model.snapshot().owner);
        assertEquals(LearningSessionViewModel.Phase.EXITED, model.state().getValue().phase); assertTrue(repository.canceled > 0);
    }
    static final class Fake implements LearningSessionRepository {
        List<Callback> calls = new ArrayList<>(); List<Boolean> starts = new ArrayList<>(); List<String> events = new ArrayList<>(); String vocabulary; int canceled;
        public Cancelable load(String owner, String event, String vocabulary, String session, boolean start, Callback callback) {
            calls.add(callback); starts.add(start); events.add(event); this.vocabulary = vocabulary; return () -> canceled++;
        }
        Callback latest() { return calls.get(calls.size() - 1); }
    }
}
