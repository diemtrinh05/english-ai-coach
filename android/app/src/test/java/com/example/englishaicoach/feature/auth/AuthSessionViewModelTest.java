package com.example.englishaicoach.feature.auth;

import static org.junit.Assert.*;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.domain.repository.AuthSessionRepository;
import com.example.englishaicoach.domain.usecase.RestoreSessionUseCase;
import org.junit.*;
import java.util.*;

public final class AuthSessionViewModelTest {
    private SessionManager sessions;
    private FakeRepository repository;
    private AuthSessionViewModel model;
    @Before public void setup() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            public void executeOnDiskIO(Runnable runnable) { runnable.run(); }
            public void postToMainThread(Runnable runnable) { runnable.run(); }
            public boolean isMainThread() { return true; }
        });
        sessions = new SessionManager(new TokenStore() {
            TokenSession value;
            public TokenSession read() { return value; }
            public void write(TokenSession session) { value = session; }
        });
        sessions.start("a", "r");
        repository = new FakeRepository();
        model = new AuthSessionViewModel(sessions, new RestoreSessionUseCase(repository));
    }
    @After public void cleanup() { model.onCleared(); ArchTaskExecutor.getInstance().setDelegate(null); }
    @Test public void loadingThenSuccessPublishesAuthenticated() {
        assertEquals(AuthSessionViewModel.State.UNKNOWN, model.state().getValue());
        model.restore(); assertEquals(AuthSessionViewModel.State.LOADING, model.state().getValue());
        repository.complete(0, AuthSessionRepository.Result.AUTHENTICATED);
        assertEquals(AuthSessionViewModel.State.AUTHENTICATED, model.state().getValue());
    }
    @Test public void retryCancelsOldCallbackAndShowsNewResult() {
        model.restore(); model.restore(); assertEquals(1, repository.cancelled);
        repository.complete(0, AuthSessionRepository.Result.SESSION_EXPIRED);
        assertEquals(AuthSessionViewModel.State.LOADING, model.state().getValue());
        repository.complete(1, AuthSessionRepository.Result.AUTHENTICATED);
        assertEquals(AuthSessionViewModel.State.AUTHENTICATED, model.state().getValue());
    }
    @Test public void accountSwitchInvalidatesPreviousBootstrap() {
        model.restore(); sessions.start("b", "s");
        repository.complete(0, AuthSessionRepository.Result.AUTHENTICATED);
        assertEquals(AuthSessionViewModel.State.UNKNOWN, model.state().getValue());
    }
    @Test public void currentInvalidationRoutesExpiredAndOldSuccessCannotRestoreMain() {
        model.restore(); sessions.clearIfCurrent(sessions.current());
        repository.complete(0, AuthSessionRepository.Result.AUTHENTICATED);
        assertEquals(AuthSessionViewModel.State.SESSION_EXPIRED, model.state().getValue());
    }
    @Test public void clearingViewModelDetachesListenerAndCancelsNetwork() {
        model.restore(); model.onCleared(); assertEquals(1, repository.cancelled);
        repository.complete(0, AuthSessionRepository.Result.AUTHENTICATED); sessions.clear();
        assertEquals(AuthSessionViewModel.State.LOADING, model.state().getValue());
    }
    @Test public void transientErrorRetainsSessionAndAllowsRetry() {
        model.restore(); repository.complete(0, AuthSessionRepository.Result.ERROR);
        assertEquals(AuthSessionViewModel.State.ERROR, model.state().getValue()); assertNotNull(sessions.current());
        model.restore(); repository.complete(1, AuthSessionRepository.Result.AUTHENTICATED);
        assertEquals(AuthSessionViewModel.State.AUTHENTICATED, model.state().getValue());
    }
    private static final class FakeRepository implements AuthSessionRepository {
        final List<Callback> callbacks = new ArrayList<>(); int cancelled;
        public Cancelable restore(Callback callback) { callbacks.add(callback); return () -> cancelled++; }
        void complete(int index, Result result) { callbacks.get(index).completed(result); }
    }
}
