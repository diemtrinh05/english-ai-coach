package com.example.englishaicoach.core.auth;

import androidx.annotation.Nullable;

public final class SessionManager {
    private final TokenStore store;
    public interface Listener { void onSessionChanged(); }
    private final java.util.Set<Listener> listeners = new java.util.HashSet<>();

    public synchronized void addListener(Listener listener) { listeners.add(listener); }
    public synchronized void removeListener(Listener listener) { listeners.remove(listener); }
    private void changed() {
        for (Listener listener : new java.util.ArrayList<>(listeners)) listener.onSessionChanged();
    }

    public SessionManager(TokenStore store) {
        this.store = store;
    }

    @Nullable public synchronized TokenSession current() {
        return store.read();
    }

    public synchronized void start(String accessToken, String refreshToken) {
        store.write(TokenSession.start(accessToken, refreshToken));
        changed();
    }

    public synchronized void clear() {
        store.write(null);
        changed();
    }

    public synchronized boolean isCurrentSession(String sessionId) {
        TokenSession current = store.read();
        return current != null && current.sessionId().equals(sessionId);
    }

    public synchronized boolean updateAccessIfCurrent(TokenSession expected,
                                                       String accessToken) {
        TokenSession current = store.read();
        if (!expected.equals(current)) return false;
        store.write(current.withAccessToken(accessToken));
        return true;
    }

    public synchronized boolean updateTokensIfCurrent(TokenSession expected,
            String accessToken, String refreshToken) {
        if (!expected.equals(store.read())) return false;
        store.write(expected.withTokens(accessToken, refreshToken));
        return true;
    }

    public synchronized void clearIfCurrent(TokenSession expected) {
        if (expected.equals(store.read())) {
            store.write(null);
            changed();
        }
    }
}
