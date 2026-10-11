package com.example.englishaicoach;

import static org.junit.Assert.*;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.englishaicoach.core.auth.AuthTestDependencies;
import com.example.englishaicoach.core.auth.EncryptedTokenStore;
import com.example.englishaicoach.core.auth.TokenSession;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class AuthNavigationInstrumentedTest {
    private MainActivity launch() {
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        Intent intent = new Intent(instrumentation.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity activity = (MainActivity) instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync(); return activity;
    }
    private void finish(MainActivity activity) {
        activity.runOnUiThread(activity::finish);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    @Test public void freshInstallShowsVietnameseLoginBoundaryAndHidesTabs() throws Exception {
        try (AuthTestDependencies fixture = new AuthTestDependencies(false)) {
            MainActivity activity = launch();
            try {
                assertEquals(View.GONE, activity.findViewById(R.id.main_navigation).getVisibility());
                assertEquals(activity.getString(R.string.auth_login_title),
                        ((TextView) activity.findViewById(R.id.auth_title)).getText().toString());
                assertEquals("AUTH_BOUNDARY", activity.getSupportFragmentManager().findFragmentById(R.id.main_content).getTag());
            } finally { finish(activity); }
        }
    }
    @Test public void invalidationClearsPrivateBackStackAndCannotGoBackIntoMain() throws Exception {
        try (AuthTestDependencies fixture = new AuthTestDependencies(true)) {
            MainActivity activity = launch();
            try {
                assertEquals(View.VISIBLE, activity.findViewById(R.id.main_navigation).getVisibility());
                activity.runOnUiThread(activity::openVocabulary);
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                fixture.sessions.clear();
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                assertEquals(0, activity.getSupportFragmentManager().getBackStackEntryCount());
                assertEquals(View.GONE, activity.findViewById(R.id.main_navigation).getVisibility());
                assertEquals(activity.getString(R.string.auth_expired),
                        ((TextView) activity.findViewById(R.id.auth_message)).getText().toString());
            } finally { finish(activity); }
        }
    }
    @Test public void encryptedSessionSurvivesNewStorageInstanceWithoutPlaintextEntry() {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        EncryptedTokenStore store = new EncryptedTokenStore(context);
        TokenSession prior = store.read();
        try {
            TokenSession snapshot = TokenSession.start("test-access", "test-refresh");
            store.write(snapshot);
            assertEquals(snapshot, new EncryptedTokenStore(context).read());
            String envelope = context.getSharedPreferences("auth_credentials", 0).getString("encrypted_session", "");
            assertFalse(envelope.contains("test-access")); assertFalse(envelope.contains("test-refresh"));
        } finally { store.write(prior); }
    }
    @Test public void activityRecreationKeepsRestoredSessionAndSecondaryRoute() throws Exception {
        try (AuthTestDependencies fixture = new AuthTestDependencies(true)) {
            MainActivity activity = launch();
            var instrumentation = InstrumentationRegistry.getInstrumentation();
            activity.runOnUiThread(() -> ((com.google.android.material.bottomnavigation.BottomNavigationView)
                    activity.findViewById(R.id.main_navigation)).setSelectedItemId(R.id.nav_learn));
            instrumentation.waitForIdleSync();
            activity.runOnUiThread(activity::openVocabulary);
            instrumentation.waitForIdleSync();
            activity.runOnUiThread(activity::recreate);
            instrumentation.waitForIdleSync();
            MainActivity[] recreated = new MainActivity[1];
            instrumentation.runOnMainSync(() -> {
                for (android.app.Activity candidate : androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
                        .getInstance().getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED)) {
                    if (candidate instanceof MainActivity) recreated[0] = (MainActivity) candidate;
                }
            });
            assertNotNull(recreated[0]);
            try {
                assertEquals(com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED,
                        recreated[0].authState().getValue());
                assertEquals(View.GONE, recreated[0].findViewById(R.id.main_navigation).getVisibility());
                assertEquals("VOCABULARY_LIST", recreated[0].getSupportFragmentManager()
                        .findFragmentById(R.id.main_content).getTag());
                assertEquals(1, recreated[0].getSupportFragmentManager().getBackStackEntryCount());
            } finally { finish(recreated[0]); }
        }
    }
}
