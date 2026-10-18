package com.example.englishaicoach.feature.learning;

import static org.junit.Assert.*;
import androidx.arch.core.executor.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.*;
import okhttp3.ResponseBody;
import org.junit.*;
import java.util.*;

public final class LearningAttemptViewModelTest {
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
        return new LearningSessionViewModel(sessions, new OpenLearningSessionUseCase(repository),
                new SubmitLearningAttemptUseCase(repository), Runnable::run, () -> now, ID, saved);
    }
    void activate() { model.setAuthenticated(true); model.setOnline(true); model.resume(); }
    void loaded() { activate(); repository.load.completed(new LearningSessionRepository.Result(
        new LearningSession(ID,"MIXED","2026-10-18T00:00:00Z",null),
        new VocabularyItem(ID,"word",null,"từ",null,"NOUN","A1",List.of(),null,List.of()),null)); model.reveal(); }
    ApiError error(int status, String code) {
        return new ApiErrorParser().parse(status, ResponseBody.create(null,"{\"code\":\""+code+"\"}"));
    }
    void pending() { loaded(); now=2500; model.selectQuality(4); }
    void success() { repository.submit.completed(new LearningAttempt(ID,false,4,"2026-10-19T00:00:00Z",1),null); }
    @After public void cleanup() { model.onCleared(); ArchTaskExecutor.getInstance().setDelegate(null); }
    @Test public void selectionFreezesTimeQualityEventAndDoubleTapDoesNotSend() {
        pending(); var body=repository.requests.get(0); now=9900; model.selectQuality(0); model.retry();
        assertEquals(1,repository.requests.size()); assertEquals(2500,body.responseTimeMs); assertEquals(4,body.answerQuality);
        assertEquals("FLASHCARD",body.attemptType); assertEquals(2500,model.elapsedMs()); assertFalse(model.backToFront());
    }
    @Test public void allSixQualitiesDispatchTheirExactFrozenOperationFromFreshBackState() {
        for (int quality = 0; quality <= 5; quality++) {
            model.onCleared(); repository = new Fake(); now = 0; model = create(null);
            loaded(); assertEquals(LearningSessionViewModel.Phase.BACK, model.state().getValue().phase);
            String startEvent = model.snapshot().event;
            now = 1200 + quality; model.selectQuality(quality);
            assertEquals(1, repository.requests.size());
            LearningAttempt.Request request = repository.requests.get(0);
            assertEquals(quality, request.answerQuality); assertEquals(1200 + quality, request.responseTimeMs);
            assertEquals(ID, request.sessionId); assertEquals(ID, request.vocabularyId);
            assertEquals("FLASHCARD", request.attemptType);
            assertNotEquals(startEvent, request.eventId); assertEquals(request.eventId, UUID.fromString(request.eventId).toString());
            now = 90000; model.selectQuality((quality + 1) % 6); model.retry();
            assertEquals(1, repository.requests.size()); assertEquals(1200 + quality, model.elapsedMs());
            assertNull(model.feedback()); assertEquals(LearningSessionViewModel.Phase.SUBMITTING, model.state().getValue().phase);
            repository.submit.completed(null, error(503, "")); model.retry();
            assertEquals(2, repository.requests.size()); assertSame(request, repository.requests.get(1));
            assertEquals(quality, request.answerQuality); assertEquals(1200 + quality, request.responseTimeMs);
            assertEquals(request.eventId, model.snapshot().attempt.eventId);
        }
    }
    @Test public void timeoutRetryPreservesExactImmutableObject() {
        pending(); var body=repository.requests.get(0); repository.submit.completed(null,error(0,"")); now=20000; model.retry();
        assertSame(body,repository.requests.get(1)); assertEquals(2,repository.requests.size());
    }
    @Test public void rotationCancelsInFlightAndOnlyExplicitRetry() {
        pending(); var callback=repository.submit; model.pause(); model.resume(); callback.completed(new LearningAttempt(ID,true,4,"now",1),null);
        assertEquals(1,repository.requests.size()); assertEquals(LearningSessionViewModel.Phase.PENDING,model.state().getValue().phase);
        model.retry(); assertEquals(2,repository.requests.size());
    }
    @Test public void restoredPendingDoesNotGetSessionOrAutomaticallyPost() {
        pending(); var saved=model.snapshot(); model.onCleared(); model=create(saved); activate();
        assertEquals(1,repository.loads); assertEquals(1,repository.requests.size()); model.retry();
        assertSame(saved.attempt,repository.requests.get(1)); assertEquals(1,repository.loads);
    }
    @Test public void acceptedResultIsServerAuthoritativeAndTerminal() {
        pending(); success(); assertFalse(model.feedback().isCorrect); assertEquals(1,model.feedback().intervalDays);
        model.selectQuality(5); model.retry(); assertEquals(1,repository.requests.size());
        assertEquals(LearningSessionViewModel.Phase.FEEDBACK,model.state().getValue().phase);
    }
    @Test public void acceptedMarkerSurvivesRecreationWithoutFakeFeedbackOrResubmit() {
        pending(); success(); var saved=model.snapshot(); model.onCleared(); model=create(saved); activate(); model.retry(); model.selectQuality(1);
        assertEquals(LearningSessionViewModel.Phase.ACCEPTED,model.state().getValue().phase);
        assertNull(model.feedback()); assertEquals(1,repository.requests.size()); assertEquals(1,repository.loads);
    }
    @Test public void offlineInFlightDoesNotQueueOrResumePost() {
        pending(); model.setOnline(false); model.retry(); model.selectQuality(0); model.setOnline(true);
        assertEquals(1,repository.requests.size()); assertEquals(LearningSessionViewModel.Phase.PENDING,model.state().getValue().phase);
        model.retry(); assertEquals(2,repository.requests.size());
    }
    @Test public void otherAccountNeverRestoresPendingOrReceivesLateResult() {
        pending(); var callback=repository.submit; var saved=model.snapshot(); sessions.start("B","R"); success();
        assertNull(model.snapshot().attempt); assertEquals(LearningSessionViewModel.Phase.EXPIRED,model.state().getValue().phase);
        model.onCleared(); model=create(saved); activate(); model.retry(); assertEquals(1,repository.requests.size());
    }
    @Test public void concurrentConflictNeedsBothRefreshesAndExplicitNewAction() {
        pending(); String event=repository.requests.get(0).eventId; repository.submit.completed(null,error(409,"CONCURRENT_UPDATE"));
        model.selectQuality(1); assertEquals(1,repository.requests.size()); model.retry(); assertEquals(1,repository.refreshes);
        repository.refresh.completed(true,null); repository.load.completed(new LearningSessionRepository.Result(
            new LearningSession(ID,"MIXED","now",null),new VocabularyItem(ID,"w",null,"từ",null,"NOUN","A1",List.of(),null,List.of()),null));
        assertEquals(1,repository.requests.size()); now=2700; model.selectQuality(1);
        assertEquals(2,repository.requests.size()); assertNotEquals(event,repository.requests.get(1).eventId);
        assertEquals(200,repository.requests.get(1).responseTimeMs);
    }
    @Test public void failedConflictRefreshKeepsOldOperationBlocked() {
        pending(); repository.submit.completed(null,error(409,"CONCURRENT_UPDATE")); model.retry(); repository.refresh.completed(false,error(503,""));
        model.selectQuality(0); assertEquals(1,repository.requests.size()); assertEquals(LearningSessionViewModel.Phase.CONCURRENT,model.state().getValue().phase);
    }
    @Test public void closedConflictRefreshDoesNotCreateAnotherAttempt() {
        pending(); repository.submit.completed(null,error(409,"CONCURRENT_UPDATE")); model.retry(); repository.refresh.completed(false,null);
        model.selectQuality(1); model.retry(); assertEquals(1,repository.requests.size()); assertEquals(LearningSessionViewModel.Phase.CLOSED,model.state().getValue().phase);
    }
    @Test public void reuseConflictNeverPostsAgainEvenAfterRefresh() {
        pending(); repository.submit.completed(null,error(409,"IDEMPOTENCY_KEY_REUSE")); model.retry(); repository.refresh.completed(true,null);
        model.selectQuality(0); assertEquals(1,repository.requests.size()); assertEquals(LearningSessionViewModel.Phase.REUSE,model.state().getValue().phase);
    }
    @Test public void overflowNeverWrapsOrClampsTimeIntoRequest() {
        loaded(); now=(long)Integer.MAX_VALUE+1; model.selectQuality(3); assertTrue(repository.requests.isEmpty());
        assertEquals(LearningSessionViewModel.Phase.OVERFLOW,model.state().getValue().phase);
    }
    @Test public void frontAndInvalidQualityNeverSubmit() {
        activate(); model.selectQuality(0); assertTrue(repository.requests.isEmpty()); loaded(); model.selectQuality(-1); model.selectQuality(6);
        assertTrue(repository.requests.isEmpty());
    }
    @Test public void validationFailureCannotBeBypassedWithRetryOrResume() {
        pending(); repository.submit.completed(null,error(400,"VALIDATION_ERROR")); model.pause(); model.resume(); model.retry();
        assertEquals(1,repository.requests.size()); assertEquals(LearningSessionViewModel.Phase.REJECTED,model.state().getValue().phase);
    }
    @Test public void rateLimitRetryKeepsEventAndRefreshKeepsAuthOwner() {
        pending(); var body=repository.requests.get(0); sessions.updateTokensIfCurrent(sessions.current(),"rotated","r");
        repository.submit.completed(null,error(429,"RATE_LIMITED")); model.retry(); assertSame(body,repository.requests.get(1));
    }
    @Test public void exitClearsPayloadAndIgnoresServerSuccess() {
        pending(); var callback=repository.submit; model.exit(); callback.completed(new LearningAttempt(ID,true,4,"now",1),null);
        assertNull(model.snapshot().attempt); assertEquals(LearningSessionViewModel.Phase.EXITED,model.state().getValue().phase);
    }
    static final class Fake implements LearningSessionRepository, LearningAttemptRepository {
        LearningSessionRepository.Callback load; LearningAttemptRepository.Callback submit; RefreshCallback refresh;
        int loads,refreshes; List<LearningAttempt.Request> requests=new ArrayList<>();
        public Cancelable load(String owner,String event,String vocabulary,String session,boolean start,LearningSessionRepository.Callback callback) {
            loads++;load=callback;return ()->{};
        }
        public Cancelable submit(String owner,LearningAttempt.Request request,LearningAttemptRepository.Callback callback) {
            requests.add(request);submit=callback;return ()->{};
        }
        public Cancelable refresh(String owner,String session,String vocabulary,RefreshCallback callback) {
            refreshes++;refresh=callback;return ()->{};
        }
    }
}
