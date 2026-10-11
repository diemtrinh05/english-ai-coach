package com.example.englishaicoach.feature.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.domain.repository.AuthSessionRepository;
import com.example.englishaicoach.domain.usecase.RestoreSessionUseCase;

public final class AuthSessionViewModel extends ViewModel {
    public enum State { UNKNOWN, LOADING, AUTHENTICATED, UNAUTHENTICATED, SESSION_EXPIRED, ERROR }
    private final MutableLiveData<State> state = new MutableLiveData<>(State.UNKNOWN);
    private final SessionManager sessions;
    private final RestoreSessionUseCase useCase;
    private final SessionManager.Listener listener;
    private AuthSessionRepository.Cancelable active;
    private final java.util.concurrent.atomic.AtomicInteger generation = new java.util.concurrent.atomic.AtomicInteger();
    private volatile boolean closed;
    public AuthSessionViewModel(SessionManager sessions, RestoreSessionUseCase useCase) {
        this.sessions = sessions;
        this.useCase = useCase;
        listener = () -> {
            generation.incrementAndGet();
            if (!closed) state.postValue(sessions.current() == null ? State.SESSION_EXPIRED : State.UNKNOWN);
        };
        sessions.addListener(listener);
    }
    public LiveData<State> state() { return state; }
    public void restore() {
        if (active != null) active.cancel();
        int requestGeneration = generation.incrementAndGet();
        state.setValue(State.LOADING);
        active = useCase.restore(result -> {
            synchronized (sessions) {
                if (!closed && requestGeneration == generation.get()) state.postValue(State.valueOf(result.name()));
            }
        });
    }
    @Override protected void onCleared() {
        synchronized (sessions) {
            closed = true;
            generation.incrementAndGet();
            if (active != null) active.cancel();
            sessions.removeListener(listener);
        }
    }
}
