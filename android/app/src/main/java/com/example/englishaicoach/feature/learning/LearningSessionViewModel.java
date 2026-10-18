package com.example.englishaicoach.feature.learning;

import androidx.lifecycle.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.model.LearningAttempt;
import com.example.englishaicoach.domain.repository.LearningSessionRepository;
import com.example.englishaicoach.domain.usecase.OpenLearningSessionUseCase;
import com.example.englishaicoach.domain.usecase.SubmitLearningAttemptUseCase;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.LongSupplier;

public final class LearningSessionViewModel extends ViewModel {
    public enum Phase { INITIAL, LOADING, FRONT, BACK, ERROR, OFFLINE, CLOSED, EXPIRED, EXITED,
        SUBMITTING, PENDING, FEEDBACK, ACCEPTED, CONCURRENT, REUSE, REFRESHING, OVERFLOW, REJECTED }
    public static final class Snapshot {
        public final String owner, event, vocabulary, server;
        public final LearningAttempt.Request attempt;
        public final boolean accepted, conflict, reuse, rejected;
        public Snapshot(String owner, String event, String vocabulary, String server) {
            this(owner, event, vocabulary, server, null, false, false, false, false);
        }
        public Snapshot(String owner, String event, String vocabulary, String server, LearningAttempt.Request attempt,
                boolean accepted, boolean conflict, boolean reuse, boolean rejected) {
            this.owner = owner; this.event = event; this.vocabulary = vocabulary; this.server = server;
            this.attempt = attempt; this.accepted = accepted; this.conflict = conflict; this.reuse = reuse;
            this.rejected = rejected;
        }
    }
    public static final class State {
        public final Phase phase;
        public final VocabularyItem card;
        public final ApiError error;
        State(Phase phase, VocabularyItem card, ApiError error) {
            this.phase = phase; this.card = card; this.error = error;
        }
    }
    private final SessionManager sessions;
    private final OpenLearningSessionUseCase useCase;
    private final SubmitLearningAttemptUseCase attempts;
    private final Executor executor;
    private final LongSupplier clock;
    private final SessionManager.Listener listener;
    private final MutableLiveData<State> state = new MutableLiveData<>();
    private String owner, event, vocabulary, server;
    private LearningSessionRepository.Cancelable active;
    private boolean connectivityKnown, fresh, sent, online, resumed, authenticated, busy, disposed, confirmed, back;
    private long generation, elapsed, runningSince;
    private boolean running;
    private LearningAttempt.Request pending;
    private LearningAttempt feedback;
    private boolean accepted, conflict, reuse, rejected;
    public LearningAttempt feedback() { return feedback; }
    public boolean hasAttempt() { return pending != null || accepted || conflict || reuse; }
    public LearningSessionViewModel(SessionManager sessions, OpenLearningSessionUseCase useCase,
            Executor executor, LongSupplier clock, String vocabulary, Snapshot restored) {
        this(sessions, useCase, null, executor, clock, vocabulary, restored);
    }
    public LearningSessionViewModel(SessionManager sessions, OpenLearningSessionUseCase useCase,
            SubmitLearningAttemptUseCase attempts, Executor executor, LongSupplier clock, String vocabulary, Snapshot restored) {
        this.sessions = sessions; this.useCase = useCase; this.executor = executor; this.clock = clock;
        this.attempts = attempts;
        if (restored == null) {
            TokenSession token = sessions.current(); owner = token == null ? null : token.sessionId();
            event = UUID.randomUUID().toString(); this.vocabulary = vocabulary; fresh = true;
        } else {
            owner = restored.owner; event = restored.event; this.vocabulary = restored.vocabulary;
            server = restored.server; sent = true;
            pending = restored.attempt; accepted = restored.accepted; conflict = restored.conflict; reuse = restored.reuse;
            rejected = restored.rejected;
        }
        state.setValue(new State(Phase.INITIAL, null, null));
        listener = () -> executor.execute(() -> { if (!disposed && !current()) expire(); });
        sessions.addListener(listener);
        if (!current()) expire();
    }
    public LiveData<State> state() { return state; }
    public Snapshot snapshot() { return new Snapshot(owner, event, vocabulary, server, pending, accepted, conflict, reuse, rejected); }
    private boolean current() { return owner != null && sessions.isCurrentSession(owner); }
    private boolean terminal() {
        Phase phase = state.getValue().phase;
        return phase == Phase.CLOSED || phase == Phase.EXPIRED || phase == Phase.EXITED;
    }
    public void setAuthenticated(boolean value) {
        authenticated = value;
        if (!value) { pauseTimer(); confirmed = false; cancel(); }
        else activate();
    }
    public void setOnline(boolean value) {
        if (connectivityKnown && online == value) return;
        connectivityKnown = true;
        online = value;
        if (!value) {
            fresh = false;
            pauseTimer(); confirmed = false; cancel();
            if (!terminal()) publish(Phase.OFFLINE, state.getValue().card, null);
        } else activate();
    }
    public void resume() { resumed = true; activate(); }
    public void pause() {
        resumed = false; pauseTimer(); confirmed = false; cancel();
    }
    private void activate() {
        if (disposed || terminal() || !resumed || !authenticated || !online || busy) return;
        if (!current()) { expire(); return; }
        if (hasAttempt()) { showAttempt(); return; }
        if (server != null) request(false);
        else if (fresh && !sent) request(true);
        else publish(Phase.ERROR, null, state.getValue().error);
    }
    public void retry() {
        if (disposed || terminal() || busy || !resumed || !authenticated || !online) return;
        if (!current()) { expire(); return; }
        if (accepted || rejected) return;
        if (conflict || reuse) { refreshConflict(); return; }
        if (pending != null) { submitPending(); return; }
        request(server == null);
    }
    private void request(boolean start) {
        pauseTimer(); confirmed = false; cancel(); busy = true;
        sent = true; fresh = false;
        long request = ++generation;
        publish(Phase.LOADING, null, null);
        active = useCase.load(owner, event, vocabulary, server, start, result -> executor.execute(() -> {
            if (disposed || !current() || request != generation || !resumed || !authenticated || !online) return;
            busy = false;
            if (result.session != null) server = result.session.sessionId;
            if (result.session != null && !result.session.open()) { publish(Phase.CLOSED, null, null); return; }
            if (result.error != null) {
                publish(result.error.status == 404 ? Phase.CLOSED : Phase.ERROR, null, result.error); return;
            }
            if (result.card == null || server == null) { publish(Phase.ERROR, null, null); return; }
            confirmed = true;
            publish(back ? Phase.BACK : Phase.FRONT, result.card, null);
            runTimer();
        }));
    }
    public void reveal() {
        if (state.getValue().phase != Phase.FRONT || !confirmed || !online) return;
        back = true; publish(Phase.BACK, state.getValue().card, null);
    }
    public boolean backToFront() {
        if (hasAttempt() || state.getValue().phase != Phase.BACK) return false;
        back = false; publish(Phase.FRONT, state.getValue().card, null); return true;
    }
    public long elapsedMs() { return elapsed + (running ? Math.max(0, clock.getAsLong() - runningSince) : 0); }
    private void runTimer() {
        if (!running && confirmed && resumed && online && authenticated) { runningSince = clock.getAsLong(); running = true; }
    }
    private void pauseTimer() { if (running) { elapsed += Math.max(0, clock.getAsLong() - runningSince); running = false; } }
    private void cancel() { generation++; busy = false; if (active != null) active.cancel(); active = null; }
    private void publish(Phase phase, VocabularyItem card, ApiError error) { state.setValue(new State(phase, card, error)); }
    private void showAttempt() {
        publish(accepted ? feedback == null ? Phase.ACCEPTED : Phase.FEEDBACK : rejected ? Phase.REJECTED : reuse ? Phase.REUSE
                : conflict ? Phase.CONCURRENT : Phase.PENDING, null, state.getValue().error);
    }
    public void selectQuality(int quality) {
        if (attempts == null || hasAttempt() || busy || disposed || !current() || !online || !resumed
                || !authenticated || !confirmed || state.getValue().phase != Phase.BACK || quality < 0 || quality > 5) return;
        pauseTimer();
        if (elapsedMs() > Integer.MAX_VALUE) { publish(Phase.OVERFLOW, null, null); confirmed = false; return; }
        pending = new LearningAttempt.Request(server, vocabulary, (int) elapsedMs(), quality, UUID.randomUUID().toString());
        submitPending();
    }
    private void submitPending() {
        if (attempts == null) return;
        pauseTimer(); cancel(); busy = true;
        long request = ++generation;
        publish(Phase.SUBMITTING, null, null);
        active = attempts.submit(owner, pending, (result, error) -> executor.execute(() -> {
            if (disposed || !current() || request != generation || !resumed || !authenticated || !online) return;
            busy = false;
            if (result != null) { accepted = true; feedback = result; publish(Phase.FEEDBACK, null, null); return; }
            conflict = error != null && error.status == 409 && error.isConcurrentUpdate();
            reuse = error != null && error.status == 409 && error.isIdempotencyKeyReuse();
            if (conflict || reuse) { showAttempt(); return; }
            boolean retryable = error == null || error.status == 0 || error.status == 429 || error.status >= 500;
            rejected = !retryable;
            publish(retryable ? Phase.PENDING : Phase.REJECTED, null, error);
        }));
    }
    private void refreshConflict() {
        if (attempts == null) return;
        cancel(); busy = true; long request = ++generation;
        publish(Phase.REFRESHING, null, null);
        active = attempts.refresh(owner, server, vocabulary, (open, error) -> executor.execute(() -> {
            if (disposed || !current() || request != generation || !resumed || !authenticated || !online) return;
            busy = false;
            if (error != null) { showAttempt(); return; }
            if (reuse) { publish(Phase.REUSE, null, null); return; }
            if (!open) { publish(Phase.CLOSED, null, null); return; }
            conflict = false; pending = null; elapsed = 0; back = true;
            request(false);
        }));
    }
    private void expire() {
        pauseTimer(); cancel(); owner = null; event = null; vocabulary = null; server = null;
        elapsed = 0; back = false;
        pending = null; feedback = null; accepted = false; conflict = false; reuse = false; rejected = false;
        publish(Phase.EXPIRED, null, null);
    }
    public void exit() {
        pauseTimer(); cancel(); owner = null; event = null; vocabulary = null; server = null;
        elapsed = 0; back = false;
        pending = null; feedback = null; accepted = false; conflict = false; reuse = false; rejected = false;
        publish(Phase.EXITED, null, null);
    }
    @Override protected void onCleared() { disposed = true; pauseTimer(); cancel(); sessions.removeListener(listener); }
}
