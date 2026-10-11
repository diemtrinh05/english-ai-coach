package com.example.englishaicoach.core.auth;

import static org.junit.Assert.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public final class SessionLifecycleTest {
    @Test public void terminalOldSessionCannotClearNewLoginOrNotifyExpiry() {
        SessionManager sessions = new SessionManager(new MemoryStore());
        AtomicInteger changes = new AtomicInteger();
        sessions.addListener(changes::incrementAndGet);
        sessions.start("a", "r");
        TokenSession old = sessions.current();
        sessions.start("b", "s");
        sessions.clearIfCurrent(old);
        assertEquals("b", sessions.current().accessToken());
        assertEquals(2, changes.get());
    }
    @Test public void rotationDoesNotExpireRouteAndDetachedObserverStaysDetached() {
        SessionManager sessions = new SessionManager(new MemoryStore());
        AtomicInteger changes = new AtomicInteger();
        SessionManager.Listener listener = changes::incrementAndGet;
        sessions.addListener(listener);
        sessions.start("a", "r");
        assertTrue(sessions.updateTokensIfCurrent(sessions.current(), "b", "s"));
        assertEquals(1, changes.get());
        sessions.clearIfCurrent(sessions.current());
        assertEquals(2, changes.get());
        sessions.removeListener(listener);
        sessions.start("c", "t");
        assertEquals(2, changes.get());
    }
    private static final class MemoryStore implements TokenStore {
        TokenSession value;
        public TokenSession read() { return value; }
        public void write(TokenSession session) { value = session; }
    }
}
