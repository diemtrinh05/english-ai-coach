package com.example.englishaicoach.feature.learning;

import static org.junit.Assert.*;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.fragment.app.Fragment;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.auth.AuthTestDependencies;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.LearningSessionRepository;
import com.example.englishaicoach.domain.usecase.OpenLearningSessionUseCase;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.*;

@RunWith(AndroidJUnit4.class)
public final class LearningSessionInstrumentedTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    MainActivity activity; AuthTestDependencies auth; Fake fake; Field field; Object previous;
    @Before public void setup() throws Exception {
        auth = new AuthTestDependencies(true); fake = new Fake();
        field = LearningDependencies.class.getDeclaredField("useCase"); field.setAccessible(true); previous = field.get(null);
        field.set(null, new OpenLearningSessionUseCase(fake));
        var i = InstrumentationRegistry.getInstrumentation(); activity = (MainActivity) i.startActivitySync(new Intent(i.getTargetContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); i.waitForIdleSync();
        ui(() -> { activity.openLearning(ID); activity.openLearning(ID); });
        for (int n = 0; n < 40 && fake.callbacks.isEmpty(); n++) { Thread.sleep(100); i.waitForIdleSync(); }
        assertEquals(1, fake.callbacks.size());
    }
    @After public void cleanup() throws Exception { if (activity != null) ui(() -> activity.finish()); field.set(null, previous); auth.close(); }
    void ui(Runnable task) { var i = InstrumentationRegistry.getInstrumentation(); i.runOnMainSync(task); i.waitForIdleSync(); }
    void success() {
        ui(() -> fake.latest().completed(new LearningSessionRepository.Result(new LearningSession(ID, "MIXED", "2026-10-18T00:00:00Z", null),
                new VocabularyItem(ID, "negotiate", "/nɪˈɡəʊʃieɪt/", "đàm phán", "discuss to agree", "VERB", "B2", List.of(), null,
                        List.of(new VocabularyItem.Example(ID, "We negotiate.", "Chúng tôi đàm phán.", "MANUAL"))), null)));
    }
    void capture(String stage) throws Exception {
        var descriptor = InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("screencap -p /data/local/tmp/AND-LEARN-001-" + stage + ".png");
        try (java.io.InputStream stream = new android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor)) { byte[] data = new byte[512]; while (stream.read(data) >= 0) { } }
    }
    void recreateFragment() {
        ui(() -> {
            Fragment old = activity.getSupportFragmentManager().findFragmentById(R.id.main_content);
            Fragment.SavedState saved = activity.getSupportFragmentManager().saveFragmentInstanceState(old);
            LearningSessionFragment replacement = LearningSessionFragment.forVocabulary(ID); replacement.setInitialSavedState(saved);
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, replacement, "LEARNING").commitNow();
        });
    }
    @Test public void frontRevealHardwareBackAndExitConfirmation() throws Exception {
        success(); assertEquals("negotiate", ((TextView) activity.findViewById(R.id.learning_word)).getText().toString());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.learning_reveal_button).getVisibility()); capture("front");
        ui(() -> activity.findViewById(R.id.learning_reveal_button).performClick()); assertEquals(View.GONE, activity.findViewById(R.id.learning_reveal_button).getVisibility()); capture("back");
        ui(() -> activity.getOnBackPressedDispatcher().onBackPressed()); assertEquals(View.VISIBLE, activity.findViewById(R.id.learning_reveal_button).getVisibility());
        ui(() -> activity.getOnBackPressedDispatcher().onBackPressed()); assertEquals("LEARNING", activity.getSupportFragmentManager().findFragmentById(R.id.main_content).getTag());
        assertEquals(1, fake.starts.stream().filter(Boolean::booleanValue).count());
    }
    @Test public void recreatedPendingStartDoesNotAutoPostAndExplicitRetryKeepsEvent() {
        String event = fake.events.get(0); recreateFragment(); assertEquals(1, fake.callbacks.size());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.learning_retry_button).getVisibility());
        ui(() -> activity.findViewById(R.id.learning_retry_button).performClick()); assertEquals(2, fake.callbacks.size()); assertEquals(event, fake.events.get(1));
    }
    @Test public void recreatedOpenSessionGetsServerAndReturnsToFront() {
        success(); ui(() -> activity.findViewById(R.id.learning_reveal_button).performClick()); recreateFragment();
        assertEquals(2, fake.callbacks.size()); assertFalse(fake.starts.get(1)); success();
        assertEquals(View.VISIBLE, activity.findViewById(R.id.learning_reveal_button).getVisibility());
    }
    @Test public void activityRotationKeepsLearningRouteWithoutDuplicateStart() throws Exception {
        success(); ui(() -> activity.findViewById(R.id.learning_reveal_button).performClick());
        ui(() -> activity.recreate());
        ui(() -> {
            for (android.app.Activity candidate : androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED))
                if (candidate instanceof MainActivity) activity = (MainActivity) candidate;
        });
        assertEquals("LEARNING", activity.getSupportFragmentManager().findFragmentById(R.id.main_content).getTag());
        assertEquals(1, fake.starts.stream().filter(Boolean::booleanValue).count());
        success(); assertEquals(View.GONE, activity.findViewById(R.id.learning_reveal_button).getVisibility());
    }
    @Test public void sessionInvalidationClearsPrivateLearningAndLateResult() {
        ui(() -> auth.sessions.clear()); success();
        assertEquals("AUTH_BOUNDARY", activity.getSupportFragmentManager().findFragmentById(R.id.main_content).getTag());
        assertEquals(0, activity.getSupportFragmentManager().getBackStackEntryCount()); assertNull(activity.findViewById(R.id.learning_word));
    }
    @Test public void closedServerSessionDisplaysUnavailableWithoutCard() {
        ui(() -> fake.latest().completed(new LearningSessionRepository.Result(new LearningSession(ID, "MIXED", "now", "ended"), null, null)));
        assertEquals(activity.getString(R.string.learning_closed), ((TextView) activity.findViewById(R.id.learning_status)).getText().toString());
        assertEquals(View.GONE, activity.findViewById(R.id.learning_word).getVisibility());
    }
    static final class Fake implements LearningSessionRepository {
        final List<Callback> callbacks = new ArrayList<>(); final List<String> events = new ArrayList<>(); final List<Boolean> starts = new ArrayList<>();
        public Cancelable load(String owner, String event, String vocabulary, String session, boolean start, Callback callback) { callbacks.add(callback); events.add(event); starts.add(start); return () -> { }; }
        Callback latest() { return callbacks.get(callbacks.size() - 1); }
    }
}
