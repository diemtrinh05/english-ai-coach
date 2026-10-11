package com.example.englishaicoach.domain.repository;

public interface AuthSessionRepository {
    enum Result { AUTHENTICATED, UNAUTHENTICATED, SESSION_EXPIRED, ERROR }
    interface Callback { void completed(Result result); }
    interface Cancelable { void cancel(); }
    Cancelable restore(Callback callback);
}
