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
import com.example.englishaicoach.data.local.EncryptedAssessmentCheckpointStore;
import com.example.englishaicoach.domain.model.Assessment;
import com.example.englishaicoach.domain.repository.AssessmentRepository;
import com.example.englishaicoach.domain.usecase.AssessmentUseCase;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.*;

@RunWith(AndroidJUnit4.class)
public final class AssessmentInstrumentedTest {
    static final String ID = "00000000-0000-0000-0000-000000000001";
    MainActivity activity;
    AuthTestDependencies auth;
    EncryptedAssessmentCheckpointStore store;
    Fake fake;
    Field field;
    Object previous;
    Context context;
    @Before public void setup() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        auth = new AuthTestDependencies(true);
        store = new EncryptedAssessmentCheckpointStore(context); store.write(null); store.bind(auth.sessions);
        field = AssessmentDependencies.class.getDeclaredField("useCase"); field.setAccessible(true); previous = field.get(null);
        fake = new Fake(); field.set(null, new AssessmentUseCase(fake));
    }
    @After public void cleanup() throws Exception {
        if (activity != null) ui(() -> activity.finish());
        store.write(null); field.set(null, previous); auth.close();
    }
    void ui(Runnable task) { var i = InstrumentationRegistry.getInstrumentation(); i.runOnMainSync(task); i.waitForIdleSync(); }
    void launch() throws Exception {
        var i = InstrumentationRegistry.getInstrumentation();
        activity = (MainActivity) i.startActivitySync(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        i.waitForIdleSync(); ui(() -> activity.openAssessment());
        for (int n = 0; n < 30 && activity.findViewById(R.id.assessment_start) == null; n++) { Thread.sleep(100); i.waitForIdleSync(); }
    }
    void startAndLoad() throws Exception {
        launch(); ui(() -> activity.findViewById(R.id.assessment_start).performClick());
        assertEquals(1, fake.starts);
        ui(() -> fake.start.completed(ID, null));
        ui(() -> fake.result.completed(new Assessment.Result(ID, "IN_PROGRESS", 7), null));
        ui(() -> fake.question.completed(new Assessment.Question(ID, "Chọn nghĩa của negotiate", List.of(" đàm phán ", "đi", "ăn", "ngủ"), "B2"), null));
    }
    void selectAndSubmit() throws Exception {
        startAndLoad();
        ui(() -> {
            TextView prompt = activity.findViewById(R.id.assessment_prompt);
            LinearLayout content = (LinearLayout) prompt.getParent();
            for (int n = 0; n < content.getChildCount(); n++) {
                if (content.getChildAt(n) instanceof RadioGroup) ((RadioGroup) content.getChildAt(n)).getChildAt(0).performClick();
            }
        });
        ui(() -> activity.findViewById(R.id.assessment_submit).performClick());
    }
    @Test public void actualKeystoreCiphertextSurvivesNewStoreAndRawPendingRoundTrips() {
        String owner = auth.sessions.current().sessionId();
        Assessment.Answer pending = new Assessment.Answer(ID, " đáp án riêng ", 123, UUID.randomUUID().toString());
        Assessment.Checkpoint checkpoint = new Assessment.Checkpoint(owner, UUID.randomUUID().toString(), ID, pending);
        store.write(checkpoint);
        String ciphertext = context.getSharedPreferences("assessment_checkpoint", Context.MODE_PRIVATE).getString("encrypted_checkpoint", "");
        assertFalse(ciphertext.contains(owner)); assertFalse(ciphertext.contains(pending.answer));
        Assessment.Checkpoint restored = new EncryptedAssessmentCheckpointStore(context).read();
        assertEquals(owner, restored.owner); assertEquals(pending.answer, restored.pending.answer);
        assertEquals(pending.eventId, restored.pending.eventId); assertEquals(123, restored.pending.responseTimeMs);
        assertNotEquals(ciphertext, new com.google.gson.Gson().toJson(checkpoint));
    }
    @Test public void logoutOutsidePlacementClearsDurableCheckpointAndSwitchAlsoClears() {
        store.write(new Assessment.Checkpoint(auth.sessions.current().sessionId(), UUID.randomUUID().toString(), ID, null));
        auth.sessions.clear(); assertNull(new EncryptedAssessmentCheckpointStore(context).read());
        auth.sessions.start("new", "refresh");
        store.write(new Assessment.Checkpoint(auth.sessions.current().sessionId(), UUID.randomUUID().toString(), ID, null));
        auth.sessions.start("other", "refresh"); assertNull(store.read());
    }
    @Test public void tamperedCiphertextIsRemovedWithoutReturningPrivateData() {
        store.write(new Assessment.Checkpoint(auth.sessions.current().sessionId(), UUID.randomUUID().toString(), ID, null));
        context.getSharedPreferences("assessment_checkpoint", Context.MODE_PRIVATE).edit().putString("encrypted_checkpoint", "broken").commit();
        assertNull(store.read()); assertFalse(context.getSharedPreferences("assessment_checkpoint", Context.MODE_PRIVATE).contains("encrypted_checkpoint"));
    }
    @Test public void serverOptionsRawOrderSubmitAndCompletedBoundaryAreRendered() throws Exception {
        selectAndSubmit(); assertEquals(1, fake.answers); assertEquals(" đàm phán ", fake.pending.answer);
        assertNotNull(store.read().pending);
        ui(() -> activity.findViewById(R.id.assessment_submit).performClick()); assertEquals(1, fake.answers);
        ui(() -> fake.answer.completed(new Assessment.Ack(false, true, "B2", 20), null));
        assertEquals(activity.getString(R.string.assessment_completed), ((TextView) activity.findViewById(R.id.assessment_status)).getText().toString());
        assertEquals(View.GONE, activity.findViewById(R.id.assessment_submit).getVisibility());
    }
    @Test public void newFragmentReadsEncryptedPendingWithoutAutoPostThenExplicitRetry() throws Exception {
        selectAndSubmit(); String event = fake.pending.eventId;
        ui(() -> activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, new AssessmentFragment(), "ASSESSMENT").commitNow());
        assertEquals(1, fake.answers);
        assertEquals(View.VISIBLE, activity.findViewById(R.id.assessment_retry).getVisibility());
        ui(() -> activity.findViewById(R.id.assessment_retry).performClick());
        assertEquals(2, fake.answers); assertEquals(event, fake.pending.eventId);
    }
    @Test public void hardwareBackRequiresConfirmationAndDoesNotCancelServer() throws Exception {
        startAndLoad(); ui(() -> activity.getOnBackPressedDispatcher().onBackPressed());
        // Đọc cửa sổ dialog thật để tránh accessibility root chưa kịp xuất hiện sau animation.
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            ui(() -> {
                boolean found = false;
                for (View root : android.view.inspector.WindowInspector.getGlobalWindowViews()) {
                    TextView positive = root.findViewById(android.R.id.button1);
                    if (positive != null && activity.getString(R.string.learning_exit).contentEquals(positive.getText())) found = true;
                }
                assertTrue(found);
            });
        } else {
            var automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            automation.waitForIdle(500, 5000);
            assertNotNull(automation.getRootInActiveWindow());
            assertFalse(automation.getRootInActiveWindow().findAccessibilityNodeInfosByText(activity.getString(R.string.learning_exit)).isEmpty());
        }
        assertEquals(1, fake.starts); assertNotNull(store.read().assessmentId);
    }
    static final class Fake implements AssessmentRepository {
        int starts, answers;
        Callback<String> start;
        Callback<Assessment.Result> result;
        Callback<Assessment.Question> question;
        Callback<Assessment.Ack> answer;
        Assessment.Answer pending;
        public Cancelable start(String owner, Assessment.Start request, Callback<String> callback) { starts++; start = callback; return () -> { }; }
        public Cancelable result(String owner, String id, Callback<Assessment.Result> callback) { result = callback; return () -> { }; }
        public Cancelable question(String owner, String id, Callback<Assessment.Question> callback) { question = callback; return () -> { }; }
        public Cancelable answer(String owner, String id, Assessment.Answer request, Callback<Assessment.Ack> callback) {
            answers++; pending = request; answer = callback; return () -> { };
        }
    }
}
