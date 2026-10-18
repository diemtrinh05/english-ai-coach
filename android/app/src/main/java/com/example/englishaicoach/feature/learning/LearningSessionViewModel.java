package com.example.englishaicoach.feature.learning;

import androidx.lifecycle.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.repository.LearningSessionRepository;
import com.example.englishaicoach.domain.usecase.OpenLearningSessionUseCase;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.LongSupplier;

public final class LearningSessionViewModel extends ViewModel {
    public enum Phase { INITIAL, LOADING, FRONT, BACK, ERROR, OFFLINE, CLOSED, EXPIRED, EXITED }
    public static final class Snapshot {
        public final String owner, event, vocabulary, server;
        public Snapshot(String owner, String event, String vocabulary, String server) {
            this.owner = owner; this.event = event; this.vocabulary = vocabulary; this.server = server;
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
    private final Executor executor;
    private final LongSupplier clock;
    private final SessionManager.Listener listener;
    private final MutableLiveData<State> state = new MutableLiveData<>();
    private String owner, event, vocabulary, server;
    private LearningSessionRepository.Cancelable active;
    private boolean connectivityKnown, fresh, sent, online, resumed, authenticated, busy, disposed, confirmed, back;
    private long generation, elapsed, runningSince;
    private boolean running;
    public LearningSessionViewModel(SessionManager sessions, OpenLearningSessionUseCase useCase,
            Executor executor, LongSupplier clock, String vocabulary, Snapshot restored) {
        this.sessions = sessions; this.useCase = useCase; this.executor = executor; this.clock = clock;
        if (restored == null) {
            TokenSession token = sessions.current(); owner = token == null ? null : token.sessionId();
            event = UUID.randomUUID().toString(); this.vocabulary = vocabulary; fresh = true;
        } else {
            owner = restored.owner; event = restored.event; this.vocabulary = restored.vocabulary;
            server = restored.server; sent = true;
        }
        state.setValue(new State(Phase.INITIAL, null, null));
        listener = () -> executor.execute(() -> { if (!disposed && !current()) expire(); });
        sessions.addListener(listener);
        if (!current()) expire();
    }
    public LiveData<State> state() { return state; }
    public Snapshot snapshot() { return new Snapshot(owner, event, vocabulary, server); }
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
        if (server != null) request(false);
        else if (fresh && !sent) request(true);
        else publish(Phase.ERROR, null, state.getValue().error);
    }
    public void retry() {
        if (disposed || terminal() || busy || !resumed || !authenticated || !online) return;
        if (!current()) { expire(); return; }
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
        if (state.getValue().phase != Phase.BACK) return false;
        back = false; publish(Phase.FRONT, state.getValue().card, null); return true;
    }
    public long elapsedMs() { return elapsed + (running ? Math.max(0, clock.getAsLong() - runningSince) : 0); }
    private void runTimer() {
        if (!running && confirmed && resumed && online && authenticated) { runningSince = clock.getAsLong(); running = true; }
    }
    private void pauseTimer() { if (running) { elapsed += Math.max(0, clock.getAsLong() - runningSince); running = false; } }
    private void cancel() { generation++; busy = false; if (active != null) active.cancel(); active = null; }
    private void publish(Phase phase, VocabularyItem card, ApiError error) { state.setValue(new State(phase, card, error)); }
    private void expire() {
        pauseTimer(); cancel(); owner = null; event = null; vocabulary = null; server = null;
        elapsed = 0; back = false; publish(Phase.EXPIRED, null, null);
    }
    public void exit() {
        pauseTimer(); cancel(); owner = null; event = null; vocabulary = null; server = null;
        elapsed = 0; back = false; publish(Phase.EXITED, null, null);
    }
    @Override protected void onCleared() { disposed = true; pauseTimer(); cancel(); sessions.removeListener(listener); }
}
