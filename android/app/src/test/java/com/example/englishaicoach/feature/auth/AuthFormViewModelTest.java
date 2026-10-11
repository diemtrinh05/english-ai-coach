package com.example.englishaicoach.feature.auth;
import static org.junit.Assert.*;
import androidx.arch.core.executor.*;
import com.example.englishaicoach.domain.repository.AuthFormsRepository;
import com.example.englishaicoach.domain.usecase.SubmitAuthFormUseCase;
import org.junit.*;
import java.util.*;

public final class AuthFormViewModelTest {
    Fake repository; AuthFormViewModel model;
    @Before public void setup() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            public void executeOnDiskIO(Runnable r) { r.run(); }
            public void postToMainThread(Runnable r) { r.run(); }
            public boolean isMainThread() { return true; }
        });
        repository = new Fake(); model = new AuthFormViewModel(new SubmitAuthFormUseCase(repository), Runnable::run);
    }
    @After public void cleanup() { model.onCleared(); ArchTaskExecutor.getInstance().setDelegate(null); }
    void send() { model.submit("Tên", "a@example.com", "password", "password", true); }
    @Test public void loginValidationPreventsNetwork() {
        model.submit("", "bad", "", "", true); assertEquals(0, repository.callbacks.size());
        assertEquals(AuthFormValidator.Error.EMAIL, model.state().getValue().errors.email);
        assertEquals(AuthFormValidator.Error.REQUIRED, model.state().getValue().errors.password);
    }
    @Test public void registrationConfirmationAndBounds() {
        model.switchMode(); model.submit(" ", "a@example.com", "short", "other", true);
        assertEquals(0, repository.callbacks.size());
        assertEquals(AuthFormValidator.Error.REQUIRED, model.state().getValue().errors.name);
        assertEquals(AuthFormValidator.Error.LENGTH, model.state().getValue().errors.password);
        assertEquals(AuthFormValidator.Error.MISMATCH, model.state().getValue().errors.confirmation);
        assertTrue(AuthFormValidator.validate(true, "Tên", "a@example.com", "abcdefgh", "abcdefgh").valid());
        assertFalse(AuthFormValidator.validate(true, "x".repeat(101), "a@example.com", "p".repeat(101), "p".repeat(101)).valid());
        assertTrue(AuthFormValidator.validate(false, "", "a@example.com", "x", "").valid());
    }
    @Test public void offlineBlocksMutation() {
        model.submit("", "a@example.com", "password", "", false);
        assertEquals(0, repository.callbacks.size()); assertEquals(AuthFormsRepository.Result.NETWORK, model.state().getValue().result);
    }
    @Test public void submittingBlocksDuplicateAndAllowsExplicitRetry() {
        send(); send(); assertEquals(1, repository.callbacks.size()); assertTrue(model.state().getValue().loading);
        repository.complete(0, AuthFormsRepository.Result.LOCKED); assertFalse(model.state().getValue().loading);
        assertEquals(AuthFormsRepository.Result.LOCKED, model.state().getValue().result);
        send(); assertEquals(2, repository.callbacks.size());
    }
    @Test public void modeSwitchCancelsAndRejectsLateCallback() {
        send(); model.switchMode(); repository.complete(0, AuthFormsRepository.Result.SUCCESS);
        assertTrue(model.state().getValue().register); assertNull(model.state().getValue().result); assertEquals(1, repository.cancelled);
    }
    @Test public void clearCancelsAndRejectsLateCallback() {
        send(); model.onCleared(); repository.complete(0, AuthFormsRepository.Result.SUCCESS);
        assertTrue(model.state().getValue().loading); assertEquals(1, repository.cancelled);
    }
    @Test public void queuedCallbackCannotOverwriteNewMode() {
        List<Runnable> queue = new ArrayList<>();
        model.onCleared(); model = new AuthFormViewModel(new SubmitAuthFormUseCase(repository), queue::add);
        send(); repository.complete(0, AuthFormsRepository.Result.ERROR);
        model.switchMode(); queue.get(0).run();
        assertTrue(model.state().getValue().register); assertNull(model.state().getValue().result);
    }
    static final class Fake implements AuthFormsRepository {
        List<Callback> callbacks = new ArrayList<>(); int cancelled;
        public Cancelable submit(boolean register, String name, String email, String password, Callback callback) {
            callbacks.add(callback); return () -> cancelled++;
        }
        void complete(int i, Result result) { callbacks.get(i).completed(result); }
    }
}
