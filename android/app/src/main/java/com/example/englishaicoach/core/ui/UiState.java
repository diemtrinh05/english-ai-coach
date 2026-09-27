package com.example.englishaicoach.core.ui;

import java.util.Objects;

public final class UiState<T> {
    public enum Status {
        INITIAL, LOADING, SUCCESS, EMPTY, ERROR, OFFLINE
    }

    private final Status status;
    private final T data;

    private UiState(Status status, T data) {
        this.status = Objects.requireNonNull(status);
        this.data = data;
    }

    public static <T> UiState<T> initial() { return new UiState<>(Status.INITIAL, null); }
    public static <T> UiState<T> loading() { return new UiState<>(Status.LOADING, null); }
    public static <T> UiState<T> success(T data) {
        return new UiState<>(Status.SUCCESS, Objects.requireNonNull(data));
    }
    public static <T> UiState<T> empty() { return new UiState<>(Status.EMPTY, null); }
    public static <T> UiState<T> error() { return new UiState<>(Status.ERROR, null); }
    public static <T> UiState<T> offline() { return new UiState<>(Status.OFFLINE, null); }

    public Status getStatus() { return status; }
    public T getData() { return data; }
}
