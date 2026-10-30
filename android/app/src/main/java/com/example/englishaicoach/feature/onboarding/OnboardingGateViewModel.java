package com.example.englishaicoach.feature.onboarding;

import androidx.lifecycle.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.OnboardingRoute;
import com.example.englishaicoach.domain.repository.OnboardingGateRepository;
import com.example.englishaicoach.domain.usecase.ResolveOnboardingUseCase;
import java.util.concurrent.Executor;

public final class OnboardingGateViewModel extends ViewModel {
    public enum Phase { LOADING, OFFLINE, ERROR, READY, EXPIRED }
    public static final class State {
        public final Phase phase;
        public final OnboardingRoute route;
        public final ApiError error;
        State(Phase phase, OnboardingRoute route, ApiError error) { this.phase = phase; this.route = route; this.error = error; }
    }
    private final SessionManager sessions;
    private final String owner;
    private final ResolveOnboardingUseCase useCase;
    private final Executor executor;
    private final SessionManager.Listener listener;
    private final MutableLiveData<State> state = new MutableLiveData<>(new State(Phase.LOADING, null, null));
    private OnboardingGateRepository.Cancelable active;
    private long generation;
    private boolean online, resumed, authenticated, busy, disposed, routed;
    public OnboardingGateViewModel(SessionManager sessions, ResolveOnboardingUseCase useCase, Executor executor) {
        this.sessions = sessions; this.useCase = useCase; this.executor = executor;
        TokenSession token = sessions.current(); owner = token == null ? null : token.sessionId();
        listener = () -> executor.execute(() -> { if (!disposed && !current()) expire(); });
        sessions.addListener(listener);
    }
    public LiveData<State> state() { return state; }
    public String owner() { return owner; }
    private boolean current() { return owner != null && sessions.isCurrentSession(owner); }
    public void setOnline(boolean value) {
        online = value;
        if (!value) { cancel(); state.setValue(new State(Phase.OFFLINE, null, null)); }
        else load();
    }
    public void setAuthenticated(boolean value) { authenticated = value; if (value) load(); else cancel(); }
    public void resume() { resumed = true; load(); }
    public void pause() { resumed = false; cancel(); }
    public void retry() { routed = false; load(); }
    public void routed() { routed = true; state.setValue(new State(Phase.LOADING, null, null)); }
    public void load() {
        if (disposed || routed || busy || !resumed || !online || !authenticated) return;
        if (!current()) { expire(); return; }
        long request = ++generation; busy = true;
        state.setValue(new State(Phase.LOADING, null, null));
        active = useCase.resolve(owner, (route, error) -> executor.execute(() -> {
            // Giữ kiểm tra owner và thông báo điều hướng trong cùng lock với logout/account switch.
            synchronized (sessions) {
                if (disposed || request != generation || !current() || !resumed || !online || !authenticated) return;
                busy = false;
                state.setValue(new State(route == null ? Phase.ERROR : Phase.READY, route, error));
            }
        }));
    }
    private void cancel() { generation++; busy = false; if (active != null) active.cancel(); active = null; }
    private void expire() { cancel(); state.setValue(new State(Phase.EXPIRED, null, null)); }
    @Override protected void onCleared() { disposed = true; cancel(); sessions.removeListener(listener); }
}
