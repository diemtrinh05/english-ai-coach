package com.example.englishaicoach.feature.assessment;

import static org.junit.Assert.*;
import androidx.arch.core.executor.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.domain.model.Assessment;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.AssessmentUseCase;
import okhttp3.ResponseBody;
import org.junit.*;
import java.util.*;

public final class AssessmentViewModelTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    static final String QUESTION = "00000000-0000-0000-0000-000000000002";
    SessionManager sessions;
    Store store;
    Fake repository;
    AssessmentViewModel model;
    long now;
    @Before public void setup() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            public void executeOnDiskIO(Runnable task) { task.run(); }
            public void postToMainThread(Runnable task) { task.run(); }
            public boolean isMainThread() { return true; }
        });
        sessions = new SessionManager(new TokenStore() {
            TokenSession value;
            public TokenSession read() { return value; }
            public void write(TokenSession token) { value = token; }
        });
        sessions.start("access", "refresh"); store = new Store(); repository = new Fake(); model = create();
    }
    AssessmentViewModel create() { return new AssessmentViewModel(sessions, store, new AssessmentUseCase(repository), Runnable::run, () -> now); }
    void activate() { model.setAuthenticated(true); model.setOnline(true); model.resume(); }
    void open() {
        activate(); model.start(); repository.start.completed(ID, null);
        result("IN_PROGRESS", 7); question(QUESTION);
    }
    void result(String status, int count) { repository.result.completed(new Assessment.Result(ID, status, count), null); }
    void question(String id) { repository.question.completed(new Assessment.Question(id, "Raw prompt", List.of(" A ", "B", "C", "D"), "C2"), null); }
    void pending() { open(); model.select(" A "); now = 1234; model.submit(); }
    ApiError error(int status, String code) { return new ApiErrorParser().parse(status, ResponseBody.create(null, "{\"code\":\"" + code + "\"}")); }
    @After public void cleanup() { model.onCleared(); ArchTaskExecutor.getInstance().setDelegate(null); }
    @Test public void introDoesNotPostUntilExplicitStartAndPersistsBeforeDispatch() {
        activate(); assertEquals(0, repository.starts); assertNull(store.value);
        repository.beforeStart = () -> assertNotNull(store.value.startEvent);
        model.start(); model.start(); assertEquals(1, repository.starts); assertEquals("INITIAL", repository.startRequest.type);
    }
    @Test public void rawSelectionTimerEventAreFrozenBeforePostAndDoubleTapBlocked() {
        repository.beforeAnswer = () -> assertSame(store.value.pending, repository.request);
        pending(); now = 9000; model.select("B"); model.submit(); model.retry();
        assertEquals(1, repository.answers); assertEquals(" A ", repository.request.answer);
        assertEquals(1234, repository.request.responseTimeMs); assertEquals(1234, model.elapsedMs());
    }
    @Test public void timeoutAnd503RetrySamePayloadWithoutResultGuard() {
        pending(); Assessment.Answer first = repository.request; int reads = repository.reads;
        repository.answer.completed(null, error(503, "ASSESSMENT_CONTENT_UNAVAILABLE")); model.retry();
        assertSame(first, repository.request); assertEquals(2, repository.answers); assertEquals(reads, repository.reads);
    }
    @Test public void coldRestoredPendingOnlyRetriesExplicitEvenWhenServerMightBeClosed() {
        pending(); Assessment.Answer first = store.value.pending; int reads = repository.reads;
        model.onCleared(); model = create(); activate();
        assertEquals(1, repository.answers); assertEquals(reads, repository.reads);
        model.retry(); assertSame(first, repository.request); assertEquals(2, repository.answers);
    }
    @Test public void restoredStartNeedsExplicitSameEvent() {
        activate(); model.start(); String event = store.value.startEvent;
        model.onCleared(); model = create(); activate(); assertEquals(1, repository.starts);
        model.retry(); assertEquals(2, repository.starts); assertEquals(event, repository.startRequest.eventId);
    }
    @Test public void restoredKnownIdReadsStateThenPersistedQuestionWithoutStart() {
        open(); model.onCleared(); model = create(); activate();
        assertEquals(1, repository.starts); result("IN_PROGRESS", 13); question(QUESTION);
        assertEquals(13, model.state().getValue().answered); assertEquals("Raw prompt", model.state().getValue().question.text);
    }
    @Test public void rotationKeepsActiveElapsedButProcessRecreationResetsUnsubmittedTimer() {
        open(); now = 100; model.pause(); now = 9100; model.resume(); result("IN_PROGRESS", 7); question(QUESTION);
        now = 9200; assertEquals(200, model.elapsedMs());
        model.onCleared(); model = create(); activate(); result("IN_PROGRESS", 7); question(QUESTION);
        now = 9250; assertEquals(50, model.elapsedMs());
    }
    @Test public void offlineAndPauseCancelWithoutAutomaticMutationReplay() {
        pending(); var callback = repository.answer;
        model.setOnline(false); model.retry(); model.setOnline(true); model.pause(); model.resume();
        callback.completed(new Assessment.Ack(true, true, "C2", 50), null);
        assertEquals(1, repository.answers); assertEquals(AssessmentViewModel.Phase.PENDING, model.state().getValue().phase);
        model.retry(); assertEquals(2, repository.answers);
    }
    @Test public void acceptedNonterminalUsesServerCountAndLoadsNextWithoutAlgorithm() {
        pending(); repository.answer.completed(new Assessment.Ack(false, false, "C2", 31), null);
        assertNull(store.value.pending); result("IN_PROGRESS", 31); question(UUID.randomUUID().toString());
        assertEquals(31, model.state().getValue().answered); assertEquals(AssessmentViewModel.Phase.QUESTION, model.state().getValue().phase);
    }
    @Test public void terminalAckDoesNotLoadNextOrComputeResultAndClearsOnlyOnExit() {
        pending(); int questions = repository.questions;
        repository.answer.completed(new Assessment.Ack(false, true, "A1", 20), null);
        assertEquals(AssessmentViewModel.Phase.COMPLETED, model.state().getValue().phase);
        model.retry(); assertEquals(questions, repository.questions); assertNotNull(store.value);
        model.exit(); assertNull(store.value);
    }
    @Test public void answeredConflictRefreshesOnlyReadsWithoutReplacementMutation() {
        pending(); repository.answer.completed(null, error(409, "ASSESSMENT_QUESTION_ALREADY_ANSWERED"));
        assertNull(store.value.pending); result("IN_PROGRESS", 8); question(UUID.randomUUID().toString());
        assertEquals(1, repository.answers); assertEquals(8, model.state().getValue().answered);
    }
    @Test public void idempotencyReuseCannotBeBypassedByRetryOrResume() {
        pending(); repository.answer.completed(null, error(409, "IDEMPOTENCY_KEY_REUSE"));
        model.retry(); model.pause(); model.resume(); model.start();
        assertEquals(1, repository.answers); assertEquals(1, repository.starts); assertEquals(AssessmentViewModel.Phase.REUSE, model.state().getValue().phase);
    }
    @Test public void unknownExistingAssessmentReportsBoundaryWithoutStartLoop() {
        activate(); model.start(); repository.start.completed(null, error(409, "ASSESSMENT_ALREADY_IN_PROGRESS"));
        model.retry(); model.start(); assertEquals(1, repository.starts); assertEquals(AssessmentViewModel.Phase.UNKNOWN, model.state().getValue().phase);
    }
    @Test public void failedCheckpointWriteBlocksBothStartAndAnswerDispatch() {
        activate(); store.fail = true; model.start(); assertEquals(0, repository.starts);
        store.fail = false; model.onCleared(); model = create(); open(); model.select("B"); store.fail = true; model.submit();
        assertEquals(0, repository.answers); assertEquals(AssessmentViewModel.Phase.STORAGE, model.state().getValue().phase);
    }
    @Test public void accountSwitchDropsLateAckAndRestoredOldCheckpoint() {
        pending(); var callback = repository.answer; sessions.start("other", "refresh");
        callback.completed(new Assessment.Ack(true, true, "C2", 50), null);
        assertEquals(AssessmentViewModel.Phase.EXPIRED, model.state().getValue().phase);
        model.onCleared(); model = create(); activate(); assertNull(store.value); assertEquals(1, repository.answers);
    }
    @Test public void refreshPreservesOwnerAndRetryIdentity() {
        pending(); Assessment.Answer first = repository.request;
        sessions.updateTokensIfCurrent(sessions.current(), "rotated", "rotatedRefresh");
        repository.answer.completed(null, error(429, "RATE_LIMITED")); model.retry(); assertSame(first, repository.request);
    }
    @Test public void overflowCannotWrapOrClampIntoRequest() {
        open(); model.select("B"); now = (long) Integer.MAX_VALUE + 1; model.submit();
        assertEquals(0, repository.answers); assertEquals(AssessmentViewModel.Phase.OVERFLOW, model.state().getValue().phase);
    }
    @Test public void localExitKeepsPendingAndIgnoresLateAck() {
        pending(); var callback = repository.answer; model.exit();
        callback.completed(new Assessment.Ack(true, true, "C2", 50), null); assertNotNull(store.value.pending);
        model.retry(); model.resume(); assertEquals(1, repository.answers);
    }
    static final class Store implements AssessmentCheckpointStore {
        Assessment.Checkpoint value; boolean fail;
        public Assessment.Checkpoint read() { return value; }
        public void write(Assessment.Checkpoint checkpoint) { if (fail) throw new IllegalStateException(); value = checkpoint; }
    }
    static final class Fake implements AssessmentRepository {
        int starts, reads, questions, answers;
        Assessment.Start startRequest; Assessment.Answer request;
        Callback<String> start; Callback<Assessment.Result> result; Callback<Assessment.Question> question; Callback<Assessment.Ack> answer;
        Runnable beforeStart = () -> { }, beforeAnswer = () -> { };
        public Cancelable start(String owner, Assessment.Start request, Callback<String> callback) {
            starts++; startRequest = request; beforeStart.run(); start = callback; return () -> { };
        }
        public Cancelable result(String owner, String id, Callback<Assessment.Result> callback) { reads++; result = callback; return () -> { }; }
        public Cancelable question(String owner, String id, Callback<Assessment.Question> callback) { questions++; question = callback; return () -> { }; }
        public Cancelable answer(String owner, String id, Assessment.Answer request, Callback<Assessment.Ack> callback) {
            answers++; this.request = request; beforeAnswer.run(); answer = callback; return () -> { };
        }
    }
}
