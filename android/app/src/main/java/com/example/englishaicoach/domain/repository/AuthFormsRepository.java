package com.example.englishaicoach.domain.repository;

public interface AuthFormsRepository {
    enum Result { SUCCESS, INVALID_CREDENTIALS, LOCKED, EMAIL_EXISTS, VALIDATION, RATE_LIMITED, NETWORK, ERROR, SUPERSEDED }
    interface Callback { void completed(Result result); }
    interface Cancelable { void cancel(); }
    Cancelable submit(boolean register, String fullName, String email, String password, Callback callback);
}
