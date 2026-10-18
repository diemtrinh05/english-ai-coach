package com.example.englishaicoach.feature.learning;
import static org.junit.Assert.*;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.*;
import org.junit.*;
import java.math.BigDecimal;
import java.util.*;
public final class LearningCompletionViewModelTest {
    LearningAttemptViewModelTest f; Fake repository;
    @Before public void setup() { f = new LearningAttemptViewModelTest(); f.setup(); repository = new Fake(); configure(); }
    void configure() { f.model.configure(new ReviewLearningUseCase(repository), "MIXED", "DETAIL"); }
    @After public void cleanup() { f.cleanup(); }
    void accepted() { f.pending(); f.success(); }
    LearningSummary summary() { return new LearningSummary(f.ID,"MIXED","2026-10-18T00:00:00Z","2026-10-18T00:01:00Z",1,new BigDecimal("0.00")); }
    void restore() { var saved = f.model.snapshot(); f.model.onCleared(); f.model = f.create(saved); configure(); f.activate(); }
    @Test public void acceptedTriggersSeparateReadAndNeverResubmitsAttempt() {
        accepted(); assertEquals(1,repository.progressCalls); assertTrue(f.model.progressLoading());
        repository.progress.completed(new VocabularyProgress(f.ID,"REVIEWING",null,new BigDecimal("2.5"),1,1,2,3),null);
        assertEquals("REVIEWING",f.model.progress().status); assertFalse(f.model.progressLoading());
        f.model.retry(); f.model.selectQuality(2); assertEquals(1,f.repository.requests.size());
    }
    @Test public void failedReadCanRetryWithoutBlockingCompletionOrResendingAttempt() {
        accepted(); repository.progress.completed(null,f.error(503,"")); assertNotNull(f.model.progressError());
        f.model.refreshProgress(); assertEquals(2,repository.progressCalls); f.model.complete();
        assertEquals(1,repository.events.size()); assertEquals(1,f.repository.requests.size());
    }
    @Test public void emptySessionCannotCompleteAndDoubleTapDispatchesOnce() {
        f.loaded(); f.model.complete(); assertTrue(repository.events.isEmpty()); f.model.selectQuality(3);
        f.repository.submit.completed(new LearningAttempt(f.ID,true,3,"2026-10-19T00:00:00Z",1),null);
        f.model.complete(); f.model.complete(); f.model.retry(); assertEquals(1,repository.events.size());
        assertNotEquals(f.model.snapshot().attempt.eventId,repository.events.get(0));
        assertNotEquals(f.model.snapshot().event,repository.events.get(0));
    }
    @Test public void timeoutExplicitRetryKeepsSameEventAndDoesNotFetchOpenSession() {
        accepted(); f.model.complete(); String event=repository.events.get(0); repository.complete.completed(null,f.error(0,""));
        f.model.retry(); assertEquals(List.of(event,event),repository.events); assertEquals(0,repository.summaryCalls);
        assertEquals(1,f.repository.loads);
    }
    @Test public void rotationDropsLateCompletionAndNeedsExplicitRetry() {
        accepted(); f.model.complete(); var late=repository.complete; f.model.pause(); f.model.resume(); late.completed(summary(),null);
        assertEquals(LearningSessionViewModel.Phase.COMPLETE_PENDING,f.model.state().getValue().phase);
        assertNull(f.model.summary()); assertEquals(1,repository.events.size()); f.model.retry(); assertEquals(2,repository.events.size());
    }
    @Test public void osSavedStatePendingRetainsPayloadAndNoAutomaticPost() {
        accepted(); f.model.complete(); String event=repository.events.get(0); restore();
        assertEquals(1,repository.events.size()); assertEquals(event,f.model.snapshot().completeEvent);
        f.model.retry(); assertEquals(List.of(event,event),repository.events); assertEquals(1,f.repository.loads);
    }
    @Test public void completedSnapshotRestoresByGetOnlyAndUsesServerSummary() {
        accepted(); f.model.complete(); repository.complete.completed(summary(),null);
        assertEquals(new BigDecimal("0.00"),f.model.summary().accuracyPercent);
        assertEquals(LearningSessionViewModel.Phase.COMPLETED,f.model.state().getValue().phase);
        restore(); assertEquals(1,repository.events.size()); assertEquals(1,repository.summaryCalls);
        repository.summary.completed(null,f.error(503,"")); f.model.retry(); assertEquals(2,repository.summaryCalls);
        repository.summary.completed(summary(),null); assertEquals(1,f.model.summary().wordsStudiedCount);
        assertEquals(1,repository.events.size());
    }
    @Test public void acceptedMarkerRestoresReadOnlyProgressWithoutAttemptReplay() {
        accepted(); restore(); assertEquals(2,repository.progressCalls); assertNull(f.model.feedback());
        assertEquals(1,f.repository.requests.size()); f.model.complete(); assertEquals(1,repository.events.size());
    }
    @Test public void reuseIsTerminalNoNewCompletionEventOrRetry() {
        accepted(); f.model.complete(); repository.complete.completed(null,f.error(409,"IDEMPOTENCY_KEY_REUSE"));
        f.model.retry(); f.model.complete(); f.model.pause(); f.model.resume(); f.model.retry();
        assertEquals(1,repository.events.size()); assertEquals(LearningSessionViewModel.Phase.COMPLETE_REJECTED,f.model.state().getValue().phase);
    }
    @Test public void offlinePendingNeverAutomaticallyPostsOnReconnect() {
        accepted(); f.model.complete(); f.model.setOnline(false); f.model.retry(); f.model.complete(); f.model.setOnline(true);
        assertEquals(1,repository.events.size()); f.model.retry(); assertEquals(2,repository.events.size());
    }
    @Test public void differentOwnerInvalidatesPendingAndBothLatePrivateCallbacks() {
        accepted(); var progress=repository.progress; f.model.complete(); var complete=repository.complete; var saved=f.model.snapshot();
        f.sessions.start("B","r"); complete.completed(summary(),null); progress.completed(null,f.error(503,""));
        assertEquals(LearningSessionViewModel.Phase.EXPIRED,f.model.state().getValue().phase);
        assertNull(f.model.summary()); assertNull(f.model.progress()); assertNull(f.model.snapshot().completeEvent);
        f.model.onCleared(); f.model=f.create(saved); configure(); f.activate(); f.model.retry(); assertEquals(1,repository.events.size());
    }
    @Test public void reviewTypeAndOriginSurviveRestoreWithoutBecomingMixed() {
        f.model.onCleared(); f.repository=new LearningAttemptViewModelTest.Fake(); f.model=f.create(null);
        f.model.configure(new ReviewLearningUseCase(repository),"REVIEW","REVIEW"); f.activate();
        var saved=f.model.snapshot(); assertEquals("REVIEW",saved.sessionType); assertEquals("REVIEW",saved.origin);
        f.model.onCleared(); f.model=f.create(saved); configure(); assertEquals("REVIEW",f.model.snapshot().sessionType);
        assertEquals("REVIEW",f.model.origin());
    }
    @Test public void localExitCancelsAndClearsPrivateCompletionState() {
        accepted(); f.model.complete(); var callback=repository.complete; f.model.exit(); callback.completed(summary(),null);
        assertNull(f.model.snapshot().completeEvent); assertNull(f.model.summary()); assertNull(f.model.progress());
        assertEquals(LearningSessionViewModel.Phase.EXITED,f.model.state().getValue().phase);
    }
    static final class Fake implements ReviewLearningRepository {
        int progressCalls,summaryCalls; List<String> events=new ArrayList<>();
        Callback<VocabularyProgress> progress; Callback<LearningSummary> complete,summary;
        public LearningSessionRepository.Cancelable reviews(String owner,Callback<List<ReviewItem>> callback) { throw new AssertionError(); }
        public LearningSessionRepository.Cancelable progress(String owner,String vocabulary,Callback<VocabularyProgress> callback) { progressCalls++;progress=callback;return ()->{}; }
        public LearningSessionRepository.Cancelable complete(String owner,String session,String event,Callback<LearningSummary> callback) { events.add(event);complete=callback;return ()->{}; }
        public LearningSessionRepository.Cancelable summary(String owner,String session,Callback<LearningSummary> callback) { summaryCalls++;summary=callback;return ()->{}; }
    }
}
