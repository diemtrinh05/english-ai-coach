package com.example.englishaicoach.feature.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.englishaicoach.domain.repository.AuthFormsRepository;
import com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase;

public final class AuthFormViewModel extends ViewModel {
    public static final class State {
        public final boolean register, loading;
        public final AuthFormValidator.Errors errors;
        public final AuthFormsRepository.Result result;
        State(boolean register, boolean loading, AuthFormValidator.Errors errors, AuthFormsRepository.Result result) {
            this.register = register; this.loading = loading; this.errors = errors; this.result = result;
        }
    }
    private final SubmitAuthFormUseCase useCase;
    private final java.util.concurrent.Executor callbackExecutor;
    private final MutableLiveData<State> state = new MutableLiveData<>(new State(false, false, null, null));
    private final java.util.concurrent.atomic.AtomicLong generation = new java.util.concurrent.atomic.AtomicLong();
    private volatile boolean closed;
    private AuthFormsRepository.Cancelable active;
    public AuthFormViewModel(SubmitAuthFormUseCase useCase, java.util.concurrent.Executor callbackExecutor) {
        this.useCase = useCase; this.callbackExecutor = callbackExecutor;
    }
    public LiveData<State> state() { return state; }
    public synchronized void switchMode() {
        generation.incrementAndGet();
        if (active != null) active.cancel();
        State current = state.getValue();
        state.setValue(new State(!current.register, false, null, null));
    }
    public synchronized void submit(String name, String email, String password, String confirmation, boolean online) {
        State current = state.getValue();
        if (closed || current.loading) return;
        AuthFormValidator.Errors errors = AuthFormValidator.validate(current.register, name, email, password, confirmation);
        if (!errors.valid()) { state.setValue(new State(current.register, false, errors, null)); return; }
        if (!online) { state.setValue(new State(current.register, false, null, AuthFormsRepository.Result.NETWORK)); return; }
        long request = generation.incrementAndGet();
        state.setValue(new State(current.register, true, null, null));
        active = useCase.submit(current.register, name, email, password, result -> {
            callbackExecutor.execute(() -> {
                synchronized (AuthFormViewModel.this) {
                    if (!closed && request == generation.get()) state.setValue(new State(current.register, false, null, result));
                }
            });
        });
    }
    @Override protected synchronized void onCleared() {
        closed = true; generation.incrementAndGet();
        if (active != null) active.cancel();
    }
}
