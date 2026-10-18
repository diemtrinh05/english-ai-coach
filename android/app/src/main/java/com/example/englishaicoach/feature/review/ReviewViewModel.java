package com.example.englishaicoach.feature.review;
import androidx.lifecycle.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.ReviewItem;
import com.example.englishaicoach.domain.repository.LearningSessionRepository;
import com.example.englishaicoach.domain.usecase.ReviewLearningUseCase;
import java.util.*;
import java.util.concurrent.Executor;
public final class ReviewViewModel extends ViewModel {
    public enum Phase { INITIAL, LOADING, READY, EMPTY, ERROR, OFFLINE, EXPIRED }
    public static final class State {
        public final Phase phase; public final List<ReviewItem> items; public final ApiError error;
        State(Phase phase, List<ReviewItem> items, ApiError error) { this.phase = phase; this.items = items; this.error = error; }
    }
    private final SessionManager sessions;
    private final ReviewLearningUseCase useCase;
    private final Executor executor;
    private final SessionManager.Listener listener;
    private final String owner;
    private final MutableLiveData<State> state = new MutableLiveData<>(new State(Phase.INITIAL, List.of(), null));
    private boolean online, authenticated, resumed, busy, disposed;
    private long generation;
    private LearningSessionRepository.Cancelable active;
    public ReviewViewModel(SessionManager sessions, ReviewLearningUseCase useCase, Executor executor) {
        this.sessions = sessions; this.useCase = useCase; this.executor = executor;
        TokenSession token = sessions.current(); owner = token == null ? null : token.sessionId();
        listener = () -> executor.execute(() -> { if (!current()) expire(); });
        sessions.addListener(listener);
    }
    public LiveData<State> state() { return state; }
    public boolean canOpen() { return current() && authenticated && resumed && online && state.getValue().phase == Phase.READY; }
    private boolean current() { return owner != null && sessions.isCurrentSession(owner); }
    public void authenticated(boolean value) { authenticated = value; if (!value) cancel(); else refresh(); }
    public void online(boolean value) {
        if (online == value) return; online = value;
        if (!value) { cancel(); state.setValue(new State(Phase.OFFLINE, List.of(), null)); } else refresh();
    }
    public void resume() { resumed = true; refresh(); }
    public void pause() { resumed = false; cancel(); }
    public void refresh() {
        if (disposed || busy || !online || !authenticated || !resumed) return;
        if (!current()) { expire(); return; }
        busy = true; long request = ++generation;
        state.setValue(new State(Phase.LOADING, List.of(), null));
        active = useCase.reviews(owner, (items, error) -> executor.execute(() -> {
            if (disposed || !current() || request != generation || !online || !authenticated || !resumed) return;
            busy = false;
            state.setValue(new State(error != null || items == null ? Phase.ERROR : items.isEmpty() ? Phase.EMPTY : Phase.READY,
                    items == null ? List.of() : List.copyOf(items), error));
        }));
    }
    private void cancel() { generation++; busy = false; if (active != null) active.cancel(); active = null; }
    private void expire() { cancel(); state.setValue(new State(Phase.EXPIRED, List.of(), null)); }
    @Override protected void onCleared() { disposed = true; cancel(); sessions.removeListener(listener); }
}
