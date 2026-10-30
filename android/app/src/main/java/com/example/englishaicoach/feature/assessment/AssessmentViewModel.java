package com.example.englishaicoach.feature.assessment;

import androidx.lifecycle.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.Assessment;
import com.example.englishaicoach.domain.model.OnboardingRoute;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.AssessmentUseCase;
import com.example.englishaicoach.domain.usecase.ResolveOnboardingUseCase;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.LongSupplier;

public final class AssessmentViewModel extends ViewModel {
    public enum Phase { INTRO, LOADING, QUESTION, SUBMITTING, PENDING, ERROR, OFFLINE, COMPLETED, ROUTE, CLOSED, UNKNOWN, REUSE, REJECTED, STORAGE, OVERFLOW, EXPIRED }
    public static final class State {
        public final Phase phase;
        public final Assessment.Question question;
        public final ApiError error;
        public final int answered;
        public final String selected;
        public final Assessment.Result result;
        public final OnboardingRoute route;
        State(Phase phase, Assessment.Question question, ApiError error, int answered, String selected,
                Assessment.Result result, OnboardingRoute route) {
            this.phase = phase; this.question = question; this.error = error; this.answered = answered; this.selected = selected;
            this.result = result; this.route = route;
        }
    }
    private final SessionManager sessions;
    private final AssessmentCheckpointStore store;
    private final AssessmentUseCase useCase;
    private final ResolveOnboardingUseCase gate;
    private final Executor executor;
    private final LongSupplier clock;
    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final SessionManager.Listener listener;
    private Assessment.Checkpoint checkpoint;
    private Assessment.Result result;
    private OnboardingRoute route;
    private final String owner;
    private AssessmentRepository.Cancelable active;
    private boolean resumed, online, authenticated, busy, disposed, confirmed, running, exited;
    private long generation, elapsed, since;
    private int answered;
    private String timedQuestion, selected;
    public AssessmentViewModel(SessionManager sessions, AssessmentCheckpointStore store, AssessmentUseCase useCase, ResolveOnboardingUseCase gate,
            Executor executor, LongSupplier clock) {
        this.sessions = sessions; this.store = store; this.useCase = useCase; this.gate = gate; this.executor = executor; this.clock = clock;
        synchronized (sessions) {
            TokenSession token = sessions.current(); owner = token == null ? null : token.sessionId();
        }
        publish(Phase.INTRO, null, null);
        try {
            synchronized (sessions) {
                checkpoint = store.read();
                if (!current()) checkpoint = null;
                else if (checkpoint != null && !checkpoint.owner.equals(owner)) { store.write(null); checkpoint = null; }
            }
        } catch (RuntimeException failure) { publish(Phase.STORAGE, null, null); }
        listener = () -> executor.execute(() -> { if (!disposed && !current()) expire(); });
        sessions.addListener(listener);
        if (!current()) expire();
    }
    public LiveData<State> state() { return state; }
    public String owner() { return owner; }
    private boolean current() { return owner != null && sessions.isCurrentSession(owner); }
    private boolean allowed() { return !disposed && !exited && current() && resumed && authenticated && online && !busy; }
    private boolean terminal() {
        Phase p = state.getValue().phase;
        return p == Phase.COMPLETED || p == Phase.ROUTE || p == Phase.CLOSED || p == Phase.UNKNOWN || p == Phase.REUSE
                || p == Phase.REJECTED || p == Phase.STORAGE || p == Phase.OVERFLOW || p == Phase.EXPIRED;
    }
    public void setAuthenticated(boolean value) {
        authenticated = value;
        if (!value) { pauseTimer(); confirmed = false; cancel(); }
        else activate();
    }
    public void setOnline(boolean value) {
        if (online == value) return;
        online = value;
        if (!value) { pauseTimer(); confirmed = false; cancel(); if (!terminal() || state.getValue().phase == Phase.COMPLETED) publish(Phase.OFFLINE, null, null); }
        else activate();
    }
    public void resume() { resumed = true; activate(); }
    public void pause() { resumed = false; pauseTimer(); confirmed = false; cancel(); }
    private void activate() {
        if (!allowed() || terminal()) return;
        if (checkpoint == null) { publish(Phase.INTRO, null, null); return; }
        if (checkpoint.pending != null || checkpoint.assessmentId == null) { publish(Phase.PENDING, null, null); return; }
        refresh();
    }
    private boolean persist(Assessment.Checkpoint value) {
        synchronized (sessions) {
            if (!current()) { expire(); return false; }
            try { store.write(value); checkpoint = value; return true; }
            catch (RuntimeException failure) { pauseTimer(); cancel(); publish(Phase.STORAGE, null, null); return false; }
        }
    }
    public void start() {
        if (!allowed() || terminal() || checkpoint != null) return;
        if (persist(new Assessment.Checkpoint(owner, UUID.randomUUID().toString(), null, null))) sendStart();
    }
    public void retry() {
        if (!allowed() || terminal()) return;
        if (checkpoint == null) { publish(Phase.INTRO, null, null); return; }
        if (checkpoint.pending != null) sendAnswer();
        else if (checkpoint.assessmentId == null) sendStart();
        else refresh();
    }
    private long begin(Phase phase) {
        pauseTimer(); confirmed = false; cancel(); busy = true;
        publish(phase, null, null); return ++generation;
    }
    private boolean accepts(long id) { return !disposed && current() && id == generation && resumed && authenticated && online; }
    private void sendStart() {
        long id = begin(Phase.SUBMITTING);
        active = useCase.start(owner, checkpoint.startEvent, (server, error) -> executor.execute(() -> {
            if (!accepts(id)) return; busy = false;
            if (server != null) {
                if (persist(new Assessment.Checkpoint(owner, checkpoint.startEvent, server, null))) refresh();
            } else fail(error, true);
        }));
    }
    private void refresh() {
        long id = begin(Phase.LOADING);
        active = useCase.result(owner, checkpoint.assessmentId, (result, error) -> executor.execute(() -> {
            if (!accepts(id)) return;
            if (result == null) { busy = false; fail(error, false); return; }
            answered = result.answered;
            if (!"IN_PROGRESS".equals(result.status)) {
                busy = false;
                if ("COMPLETED".equals(result.status) && result.level == null) { publish(Phase.ERROR, null, null); return; }
                this.result = result;
                publish("COMPLETED".equals(result.status) ? Phase.COMPLETED : Phase.CLOSED, null, null); return;
            }
            active = useCase.question(owner, checkpoint.assessmentId, (question, failure) -> executor.execute(() -> {
                if (!accepts(id)) return; busy = false;
                if (question == null) { fail(failure, false); return; }
                if (!question.id.equals(timedQuestion)) { elapsed = 0; selected = null; timedQuestion = question.id; }
                confirmed = true; publish(Phase.QUESTION, question, null); runTimer();
            }));
        }));
    }
    public void select(String value) {
        State current = state.getValue();
        if (!allowed() || !confirmed || current.phase != Phase.QUESTION || !current.question.options.contains(value)) return;
        selected = value; publish(Phase.QUESTION, current.question, null);
    }
    public void submit() {
        State current = state.getValue();
        if (!allowed() || !confirmed || current.phase != Phase.QUESTION || selected == null || checkpoint.pending != null) return;
        pauseTimer();
        if (elapsedMs() > Integer.MAX_VALUE) { confirmed = false; publish(Phase.OVERFLOW, null, null); return; }
        Assessment.Answer pending = new Assessment.Answer(current.question.id, selected, (int) elapsedMs(), UUID.randomUUID().toString());
        if (persist(new Assessment.Checkpoint(owner, checkpoint.startEvent, checkpoint.assessmentId, pending))) sendAnswer();
    }
    private void sendAnswer() {
        long id = begin(Phase.SUBMITTING);
        active = useCase.answer(owner, checkpoint.assessmentId, checkpoint.pending, (ack, error) -> executor.execute(() -> {
            if (!accepts(id)) return; busy = false;
            if (ack != null) {
                answered = ack.answered;
                if (!persist(new Assessment.Checkpoint(owner, checkpoint.startEvent, checkpoint.assessmentId, null))) return;
                // Cả terminal acknowledgment cũng phải đọc snapshot kết quả, không suy CEFR từ nextDifficulty.
                refresh();
            } else if (error != null && "ASSESSMENT_QUESTION_ALREADY_ANSWERED".equals(error.code)) {
                if (persist(new Assessment.Checkpoint(owner, checkpoint.startEvent, checkpoint.assessmentId, null))) refresh();
            } else fail(error, false);
        }));
    }
    private void fail(ApiError error, boolean start) {
        Phase phase = error != null && error.isIdempotencyKeyReuse() ? Phase.REUSE
                : start && error != null && "ASSESSMENT_ALREADY_IN_PROGRESS".equals(error.code) ? Phase.UNKNOWN
                : error != null && error.status == 404 ? Phase.CLOSED
                : error != null && (error.status == 400 || error.status == 403) ? Phase.REJECTED
                : checkpoint != null && (checkpoint.pending != null || checkpoint.assessmentId == null) ? Phase.PENDING : Phase.ERROR;
        publish(phase, null, error);
    }
    public void continueResult() {
        if (!allowed() || state.getValue().phase != Phase.COMPLETED || result == null || result.level == null) return;
        long id = begin(Phase.LOADING);
        OnboardingGateRepository.Cancelable request = gate.resolve(owner, (destination, error) -> executor.execute(() -> {
            if (!accepts(id)) return;
            busy = false;
            if (destination == null) { publish(Phase.ERROR, null, error); return; }
            if (!persist(null)) return;
            route = destination;
            publish(Phase.ROUTE, null, null);
        }));
        active = request::cancel;
    }
    public void routeConsumed() { exited = true; route = null; publish(Phase.LOADING, null, null); }
    public long elapsedMs() { return elapsed + (running ? Math.max(0, clock.getAsLong() - since) : 0); }
    private void runTimer() { if (!running && confirmed && allowed()) { since = clock.getAsLong(); running = true; } }
    private void pauseTimer() { if (running) { elapsed += Math.max(0, clock.getAsLong() - since); running = false; } }
    private void cancel() { generation++; busy = false; if (active != null) active.cancel(); active = null; }
    private void publish(Phase phase, Assessment.Question question, ApiError error) {
        // Observer có thể điều hướng ngay; không để phiên đổi giữa owner check và observer.
        synchronized (sessions) {
            if (phase != Phase.EXPIRED && !current()) {
                result = null; route = null; question = null; selected = null; phase = Phase.EXPIRED;
            }
            state.setValue(new State(phase, question, error, answered, selected, result, route));
        }
    }
    private void expire() { pauseTimer(); cancel(); checkpoint = null; result = null; route = null; selected = null; timedQuestion = null; elapsed = 0; publish(Phase.EXPIRED, null, null); }
    public void exit() {
        exited = true; pauseTimer(); cancel();
        if (state.getValue().phase == Phase.CLOSED) persist(null);
    }
    @Override protected void onCleared() { disposed = true; pauseTimer(); cancel(); sessions.removeListener(listener); }
}
