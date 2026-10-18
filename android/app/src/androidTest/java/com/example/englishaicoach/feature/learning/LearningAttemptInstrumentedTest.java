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
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.core.network.ApiErrorParser;
import com.example.englishaicoach.domain.usecase.SubmitLearningAttemptUseCase;
import okhttp3.ResponseBody;
import com.example.englishaicoach.domain.usecase.OpenLearningSessionUseCase;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.*;

@RunWith(AndroidJUnit4.class)
public final class LearningAttemptInstrumentedTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    MainActivity activity; AuthTestDependencies auth; Fake fake; Field field, attemptField; Object previous, previousAttempt; AttemptFake attempt;
    @Before public void setup() throws Exception {
        auth = new AuthTestDependencies(true); fake = new Fake(); attempt = new AttemptFake();
        attemptField = LearningDependencies.class.getDeclaredField("attemptUseCase"); attemptField.setAccessible(true); previousAttempt = attemptField.get(null);
        attemptField.set(null, new SubmitLearningAttemptUseCase(attempt));
        field = LearningDependencies.class.getDeclaredField("useCase"); field.setAccessible(true); previous = field.get(null);
        field.set(null, new OpenLearningSessionUseCase(fake));
        var i = InstrumentationRegistry.getInstrumentation(); activity = (MainActivity) i.startActivitySync(new Intent(i.getTargetContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); i.waitForIdleSync();
        ui(() -> { activity.openLearning(ID); activity.openLearning(ID); });
        for (int n = 0; n < 40 && fake.callbacks.isEmpty(); n++) { Thread.sleep(100); i.waitForIdleSync(); }
        assertEquals(1, fake.callbacks.size());
    }
    @After public void cleanup() throws Exception { if (activity != null) ui(() -> activity.finish()); field.set(null, previous); attemptField.set(null, previousAttempt); auth.close(); }
    void ui(Runnable task) { var i = InstrumentationRegistry.getInstrumentation(); i.runOnMainSync(task); i.waitForIdleSync(); }
    void success() {
        ui(() -> fake.latest().completed(new LearningSessionRepository.Result(new LearningSession(ID, "MIXED", "2026-10-18T00:00:00Z", null),
                new VocabularyItem(ID, "negotiate", "/nɪˈɡəʊʃieɪt/", "đàm phán", "discuss to agree", "VERB", "B2", List.of(), null,
                        List.of(new VocabularyItem.Example(ID, "We negotiate.", "Chúng tôi đàm phán.", "MANUAL"))), null)));
    }
    void capture(String stage) throws Exception {
        var descriptor = InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("screencap -p /data/local/tmp/AND-LEARN-002-" + stage + ".png");
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
    void select() { success(); ui(()->activity.findViewById(R.id.learning_reveal_button).performClick());
        ui(()->activity.findViewById(R.id.learning_quality_4).performClick()); }
    void accepted() { ui(()->attempt.callback.completed(new LearningAttempt(ID,true,4,"2026-11-04T00:00:00Z",17),null)); }
    @Test public void qualitySubmitLocksDoubleTapAndServerFeedbackIsVisible() throws Exception {
        select(); ui(()->activity.findViewById(R.id.learning_quality_0).performClick()); assertEquals(1,attempt.requests.size());
        assertEquals(View.GONE,activity.findViewById(R.id.learning_quality_selector).getVisibility());
        accepted(); String feedback=((TextView)activity.findViewById(R.id.learning_status)).getText().toString();
        assertTrue(feedback.contains("17")); assertTrue(feedback.contains("2026-11-04"));
        assertEquals(View.VISIBLE,activity.findViewById(R.id.learning_return_button).getVisibility()); capture("feedback");
        ui(()->activity.getOnBackPressedDispatcher().onBackPressed());
        assertFalse(InstrumentationRegistry.getInstrumentation().getUiAutomation().getRootInActiveWindow()
                .findAccessibilityNodeInfosByText(activity.getString(R.string.learning_exit)).isEmpty());
        assertEquals(1,attempt.requests.size());
    }
    @Test public void recreatedPendingRetryPreservesFrozenPayloadWithoutAutoPostOrSessionGet() {
        select(); var request=attempt.requests.get(0); recreateFragment(); assertEquals(1,attempt.requests.size()); assertEquals(1,fake.callbacks.size());
        assertEquals(View.VISIBLE,activity.findViewById(R.id.learning_retry_button).getVisibility());
        ui(()->activity.findViewById(R.id.learning_retry_button).performClick()); assertEquals(2,attempt.requests.size());
        var retry=attempt.requests.get(1); assertEquals(request.eventId,retry.eventId);assertEquals(request.responseTimeMs,retry.responseTimeMs);assertEquals(request.answerQuality,retry.answerQuality);
    }
    @Test public void acceptedMarkerRecreationNeverShowsQualityOrResubmits() {
        select();accepted();recreateFragment();assertEquals(1,attempt.requests.size());assertEquals(1,fake.callbacks.size());
        assertEquals(activity.getString(R.string.learning_attempt_recorded),((TextView)activity.findViewById(R.id.learning_status)).getText().toString());
        assertEquals(View.GONE,activity.findViewById(R.id.learning_quality_selector).getVisibility());
    }
    @Test public void reuseConflictRefreshCannotCreateReplacementEvent() {
        select(); ui(()->attempt.callback.completed(null,new ApiErrorParser().parse(409,ResponseBody.create(null,"{\"code\":\"IDEMPOTENCY_KEY_REUSE\"}"))));
        assertEquals(activity.getString(R.string.learning_attempt_reuse),((TextView)activity.findViewById(R.id.learning_status)).getText().toString());
        ui(()->activity.findViewById(R.id.learning_retry_button).performClick());ui(()->attempt.refresh.completed(true,null));
        assertEquals(1,attempt.requests.size());assertEquals(View.GONE,activity.findViewById(R.id.learning_quality_selector).getVisibility());
    }
    @Test public void concurrentRefreshRequiresExplicitNewQuality() {
        select();String event=attempt.requests.get(0).eventId;
        ui(()->attempt.callback.completed(null,new ApiErrorParser().parse(409,ResponseBody.create(null,"{\"code\":\"CONCURRENT_UPDATE\"}"))));
        ui(()->activity.findViewById(R.id.learning_retry_button).performClick()); ui(()->attempt.refresh.completed(true,null));success();
        assertEquals(1,attempt.requests.size());ui(()->activity.findViewById(R.id.learning_quality_2).performClick());
        assertEquals(2,attempt.requests.size());assertNotEquals(event,attempt.requests.get(1).eventId);
    }
    @Test public void qualityControlsUseSixDistinctVietnameseLevels() throws Exception {
        success();ui(()->activity.findViewById(R.id.learning_reveal_button).performClick());
        int[] ids={R.id.learning_quality_0,R.id.learning_quality_1,R.id.learning_quality_2,R.id.learning_quality_3,R.id.learning_quality_4,R.id.learning_quality_5};
        for(int id:ids) { View choice=activity.findViewById(id);assertTrue(choice.getHeight()>=48*activity.getResources().getDisplayMetrics().density); }
        capture("quality"); assertTrue(attempt.requests.isEmpty());
    }
    static final class AttemptFake implements LearningAttemptRepository {
        final List<LearningAttempt.Request> requests=new ArrayList<>();LearningAttemptRepository.Callback callback;RefreshCallback refresh;
        public LearningSessionRepository.Cancelable submit(String owner,LearningAttempt.Request request,LearningAttemptRepository.Callback callback) {requests.add(request);this.callback=callback;return ()->{};}
        public LearningSessionRepository.Cancelable refresh(String owner,String session,String vocabulary,RefreshCallback callback) {refresh=callback;return ()->{};}
    }
    static final class Fake implements LearningSessionRepository {
        final List<Callback> callbacks = new ArrayList<>(); final List<String> events = new ArrayList<>(); final List<Boolean> starts = new ArrayList<>();
        public Cancelable load(String owner, String event, String vocabulary, String session, boolean start, Callback callback) { callbacks.add(callback); events.add(event); starts.add(start); return () -> { }; }
        Callback latest() { return callbacks.get(callbacks.size() - 1); }
    }
}
