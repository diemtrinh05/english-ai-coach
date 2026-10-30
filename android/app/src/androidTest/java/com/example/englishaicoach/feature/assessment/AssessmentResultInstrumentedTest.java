package com.example.englishaicoach.feature.assessment;

import static org.junit.Assert.*;
import android.content.*;
import android.view.View;
import android.widget.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.auth.AuthTestDependencies;
import com.example.englishaicoach.core.network.ApiErrorParser;
import com.example.englishaicoach.data.local.EncryptedAssessmentCheckpointStore;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.*;
import com.example.englishaicoach.feature.onboarding.OnboardingGateDependencies;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

@RunWith(AndroidJUnit4.class)
public final class AssessmentResultInstrumentedTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    MainActivity activity;
    AuthTestDependencies auth;
    EncryptedAssessmentCheckpointStore store;
    AssessmentInstrumentedTest.Fake assessment;
    Gate gate;
    Settings settings;
    Field assessmentField;
    Object previousAssessment;
    Context context;
    @Before public void setup() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        settings = new Settings(); auth = new AuthTestDependencies(true, new ConfigureOnboardingUseCase(settings));
        store = new EncryptedAssessmentCheckpointStore(context); store.write(null); store.bind(auth.sessions);
        assessmentField = AssessmentDependencies.class.getDeclaredField("useCase"); assessmentField.setAccessible(true);
        previousAssessment = assessmentField.get(null); assessment = new AssessmentInstrumentedTest.Fake();
        assessmentField.set(null, new AssessmentUseCase(assessment));
        Field gateField = OnboardingGateDependencies.class.getDeclaredField("useCase"); gateField.setAccessible(true);
        gate = new Gate(); gateField.set(null, new ResolveOnboardingUseCase(gate));
    }
    @After public void cleanup() throws Exception {
        if (activity != null) ui(() -> activity.finish());
        store.write(null); assessmentField.set(null, previousAssessment); auth.close();
    }
    void ui(Runnable action) { var i = InstrumentationRegistry.getInstrumentation(); i.runOnMainSync(action); i.waitForIdleSync(); }
    void launch() throws Exception {
        var i = InstrumentationRegistry.getInstrumentation();
        activity = (MainActivity) i.startActivitySync(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        i.waitForIdleSync(); awaitTag(gate.hold ? "ONBOARDING_GATE" : gate.route == OnboardingRoute.HOME ? "HOME" : gate.route.name());
    }
    void awaitTag(String tag) throws Exception {
        var i = InstrumentationRegistry.getInstrumentation();
        for (int n = 0; n < 60; n++) {
            final boolean[] match = {false}; ui(() -> {
                var fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_content);
                match[0] = fragment != null && tag.equals(fragment.getTag());
            });
            if (match[0]) return;
            Thread.sleep(100); i.waitForIdleSync();
        }
        fail("Không đến destination " + tag);
    }
    void openResult(boolean nullableScore) throws Exception {
        store.write(new Assessment.Checkpoint(auth.sessions.current().sessionId(), UUID.randomUUID().toString(), ID, null));
        launch(); ui(() -> activity.openAssessment());
        for (int n = 0; n < 50 && assessment.result == null; n++) { Thread.sleep(100); InstrumentationRegistry.getInstrumentation().waitForIdleSync(); }
        ui(() -> assessment.result.completed(new Assessment.Result(ID, "COMPLETED", 28, 16,
                nullableScore ? null : new BigDecimal("57.14"), new Assessment.Level("B1", "Trung cấp", "Mô tả server")), null));
    }
    @Test public void resultDisplaysServerMetricsAndNewFragmentRestoresEncryptedIdWithoutPost() throws Exception {
        openResult(false);
        assertTrue(((TextView) activity.findViewById(R.id.assessment_prompt)).getText().toString().contains("B1 — Trung cấp"));
        assertEquals(0, assessment.starts + assessment.answers);
        ui(() -> activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new AssessmentFragment(), "ASSESSMENT").commitNow());
        ui(() -> assessment.result.completed(new Assessment.Result(ID, "COMPLETED", 28, 16, null,
                new Assessment.Level("B1", "Trung cấp", null)), null));
        assertEquals(View.VISIBLE, activity.findViewById(R.id.assessment_result_continue).getVisibility());
        assertTrue(((TextView) activity.findViewById(R.id.assessment_prompt)).getText().toString().contains("B1"));
        String encrypted = context.getSharedPreferences("assessment_checkpoint", Context.MODE_PRIVATE).getString("encrypted_checkpoint", "");
        assertFalse(encrypted.contains("Trung cấp")); assertNotNull(new EncryptedAssessmentCheckpointStore(context).read());
    }
    @Test public void explicitContinueWaitsForGateThenClearsBookmarkAndRoutesHome() throws Exception {
        openResult(false); gate.hold = true; int before = gate.reads;
        ui(() -> activity.findViewById(R.id.assessment_result_continue).performClick());
        assertEquals(before + 1, gate.reads); assertNotNull(store.read());
        ui(() -> gate.callback.completed(OnboardingRoute.HOME, null)); awaitTag("HOME");
        assertNull(store.read()); assertEquals(View.VISIBLE, activity.findViewById(R.id.main_navigation).getVisibility());
        assertEquals(0, assessment.starts + assessment.answers);
    }
    @Test public void missingProfileContinueRoutesSettingsAndClearsConfirmedResultId() throws Exception {
        openResult(false); gate.route = OnboardingRoute.SETTINGS;
        ui(() -> activity.findViewById(R.id.assessment_result_continue).performClick()); awaitTag("SETTINGS");
        assertNull(store.read()); assertNotNull(activity.findViewById(R.id.onboarding_next));
        assertEquals(0, assessment.starts + assessment.answers);
    }
    @Test public void profileWithoutCefrRoutesFreshIntroWithoutAutomaticStart() throws Exception {
        openResult(false); gate.route = OnboardingRoute.PLACEMENT;
        ui(() -> activity.findViewById(R.id.assessment_result_continue).performClick()); awaitTag("PLACEMENT");
        assertNull(store.read()); assertEquals(View.VISIBLE, activity.findViewById(R.id.assessment_start).getVisibility());
        assertEquals(0, assessment.starts + assessment.answers);
    }
    @Test public void missingCefrResultAndReadFailureKeepIdAndExposeOnlyReadRetry() throws Exception {
        openResult(false);
        ui(() -> activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new AssessmentFragment(), "ASSESSMENT").commitNow());
        ui(() -> assessment.result.completed(new Assessment.Result(ID, "COMPLETED", 20), null));
        assertEquals(View.VISIBLE, activity.findViewById(R.id.assessment_retry).getVisibility());
        assertEquals(View.GONE, activity.findViewById(R.id.assessment_result_continue).getVisibility());
        ui(() -> activity.findViewById(R.id.assessment_retry).performClick());
        ui(() -> assessment.result.completed(null, new ApiErrorParser().parse(503, null)));
        assertNotNull(store.read()); assertEquals(0, assessment.starts + assessment.answers);
    }
    @Test public void bootstrapFreshGateRoutesSettingsOrIntroAndReadErrorHasExplicitRetry() throws Exception {
        gate.route = OnboardingRoute.SETTINGS; launch(); assertNotNull(activity.findViewById(R.id.onboarding_next));
        ui(() -> activity.finish()); activity = null; gate.route = OnboardingRoute.PLACEMENT; launch();
        assertEquals(View.VISIBLE, activity.findViewById(R.id.assessment_start).getVisibility());
        ui(() -> activity.finish()); activity = null; gate.hold = true; launch();
        ui(() -> gate.callback.completed(null, new ApiErrorParser().parse(503, null)));
        assertEquals(View.VISIBLE, activity.findViewById(R.id.onboarding_gate_retry).getVisibility());
        int before = gate.reads; gate.hold = false; gate.route = OnboardingRoute.HOME;
        ui(() -> activity.findViewById(R.id.onboarding_gate_retry).performClick()); awaitTag("HOME");
        assertEquals(before + 1, gate.reads); assertEquals(0, assessment.starts + assessment.answers);
    }
    @Test public void queuedOldOwnerRouteCannotOpenSettingsAfterAccountSwitch() throws Exception {
        openResult(false); gate.hold = true;
        ui(() -> activity.findViewById(R.id.assessment_result_continue).performClick());
        ui(() -> {
            gate.callback.completed(OnboardingRoute.SETTINGS, null);
            gate.hold = false; gate.route = OnboardingRoute.HOME;
            auth.sessions.start("other-user", "other-refresh");
        });
        awaitTag("HOME");
        assertEquals(0, settings.loads); assertNull(store.read());
    }
    @Test public void sameOwnerActivityRecreationPreservesResultFlowAndBookmark() throws Exception {
        openResult(false); int before = gate.reads;
        String owner = auth.sessions.current().sessionId();
        ui(() -> activity.recreate());
        ui(() -> {
            for (android.app.Activity candidate : androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED))
                if (candidate instanceof MainActivity) activity = (MainActivity) candidate;
        });
        assertEquals(owner, store.read().owner); assertEquals(before, gate.reads);
        assertEquals(View.VISIBLE, activity.findViewById(R.id.assessment_result_continue).getVisibility());
        assertEquals(0, assessment.starts + assessment.answers);
    }
    static final class Gate implements OnboardingGateRepository {
        OnboardingRoute route = OnboardingRoute.HOME;
        boolean hold; int reads; Callback callback;
        public Cancelable resolve(String owner, Callback callback) {
            reads++; this.callback = callback; if (!hold) callback.completed(route, null); return () -> { };
        }
    }
    static final class Settings implements OnboardingRepository {
        int loads;
        public Cancelable load(Callback callback) {
            loads++;
            callback.completed(new Result(new OnboardingData.Snapshot(new OnboardingData.Profile("Tên", 20, "UTC", null),
                    List.of(new OnboardingData.Goal(ID, "TRAVEL", true)), new OnboardingData.Selection(List.of(ID), null)), true, false, null));
            return () -> { };
        }
        public Cancelable save(OnboardingData.Profile profile, OnboardingData.Selection selection, boolean saved, Callback callback) { return () -> { }; }
    }
}
