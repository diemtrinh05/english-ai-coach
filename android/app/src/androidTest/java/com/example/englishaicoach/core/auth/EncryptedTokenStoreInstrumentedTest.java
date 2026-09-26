package com.example.englishaicoach.core.auth;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EncryptedTokenStoreInstrumentedTest {
    @Test public void credentialsRoundTripEncryptedAndClearOnLogout() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        EncryptedTokenStore store = new EncryptedTokenStore(context);
        store.write(null);
        try {
            TokenSession session = TokenSession.start("test-access-token", "test-refresh-token");
            store.write(session);
            assertEquals(session, store.read());
            assertEquals(session, new EncryptedTokenStore(context).read());

            SharedPreferences preferences = context.getSharedPreferences("auth_credentials", Context.MODE_PRIVATE);
            String raw = preferences.getString("encrypted_session", null);
            assertNotNull(raw);
            assertFalse(raw.contains("test-access-token"));
            assertFalse(raw.contains("test-refresh-token"));

            store.write(null);
            assertNull(store.read());
            assertNull(preferences.getString("encrypted_session", null));
        } finally {
            store.write(null);
        }
    }
}
