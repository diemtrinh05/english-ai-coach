package com.example.englishaicoach.e2e;

import static org.junit.Assert.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.auth.AppSessionDependencies;
import com.example.englishaicoach.data.api.*;
import com.example.englishaicoach.data.dto.AuthFormDtos;
import com.example.englishaicoach.domain.model.LearningAttempt;
import com.example.englishaicoach.domain.model.OnboardingData;
import com.example.englishaicoach.feature.learning.LearningSessionViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import javax.net.ssl.*;
import org.junit.*;
import org.junit.runner.RunWith;
import retrofit2.Response;

/** QA-E2E-001: chỉ chạy với fixture HTTPS thật do host runner tạo. */
@RunWith(AndroidJUnit4.class)
public final class RealBackendLearningInstrumentedTest {
    private static final Gson JSON = new Gson();
    private static X509Certificate anchor;
    private MainActivity activity;
    private AppSessionDependencies app;
    private File stageFile, releaseFile;
    private String run;
    private String previousAlgorithm;
    private final JsonObject evidence = new JsonObject();

    @Before public void setup() throws Exception {
        Bundle args = InstrumentationRegistry.getArguments();
        // Không diễn giải suite mặc định không có backend fixture thành E2E PASS.
        Assume.assumeTrue("Cần explicit QA-E2E-001 real-backend runner", args.containsKey("e2eCa"));
        run = args.getString("e2eRun");
        UUID.fromString(run);
        anchor = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
                new ByteArrayInputStream(Base64.getDecoder().decode(args.getString("e2eCa"))));
        previousAlgorithm = Security.getProperty("ssl.TrustManagerFactory.algorithm");
        Security.insertProviderAt(new AnchorProvider(), 1);
        Security.setProperty("ssl.TrustManagerFactory.algorithm", "EacE2ePkix");
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        stageFile = new File(context.getCacheDir(), "qa-e2e-" + run + ".json");
        releaseFile = new File(context.getCacheDir(), "qa-e2e-" + run + ".release");
        app = AppSessionDependencies.get(context);
        app.sessions.clear();
        String password = "A!" + UUID.randomUUID();
        Response<AuthFormDtos.Response> registration = app.api.createPublicAuth(AuthFormsApi.class)
                .register(new AuthFormDtos.Register("QA E2E", "e2e-" + run + "@example.test", password)).execute();
        assertEquals(201, registration.code());
        assertNotNull(registration.body());
        assertTrue(registration.body().valid());
        app.sessions.start(registration.body().accessToken, registration.body().refreshToken);
        assertEquals(200, app.api.createExplicitRequests(OnboardingApi.class)
                .saveProfile(new OnboardingData.Profile("QA E2E", 15, "Asia/Ho_Chi_Minh", null)).execute().code());
        evidence.addProperty("user", registration.body().user.id);
        checkpoint("FIXTURE");
    }

    @After public void cleanup() throws Exception {
        if (activity != null) ui(activity::finish);
        if (app != null) app.sessions.clear();
        if (stageFile != null) Files.deleteIfExists(stageFile.toPath());
        if (releaseFile != null) Files.deleteIfExists(releaseFile.toPath());
        if (previousAlgorithm != null) {
            Security.setProperty("ssl.TrustManagerFactory.algorithm", previousAlgorithm);
            Security.removeProvider("EacE2eAnchor");
        }
        anchor = null;
    }

    @Test public void realVocabularySessionSrsProgressCompletionAndConcurrency() throws Exception {
        Bundle args = InstrumentationRegistry.getArguments();
        String word = args.getString("e2eWord"), vocabulary = args.getString("e2eVocabulary");
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        activity = (MainActivity) instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        await(() -> activity.findViewById(R.id.main_navigation) != null);
        ui(() -> ((BottomNavigationView) activity.findViewById(R.id.main_navigation)).setSelectedItemId(R.id.nav_learn));
        click(R.id.open_vocabulary);
        ui(() -> ((EditText) activity.findViewById(R.id.search_input)).setText(word));
        click(R.id.search_button);
        await(() -> {
            RecyclerView list = activity.findViewById(R.id.vocabulary_items);
            if (list == null || list.getChildCount() == 0) return false;
            android.widget.TextView label = list.getChildAt(0).findViewById(R.id.word);
            return label != null && word.equals(label.getText().toString());
        });
        ui(() -> ((RecyclerView) activity.findViewById(R.id.vocabulary_items)).getChildAt(0).performClick());
        await(() -> activity.findViewById(R.id.learn_button) != null && activity.findViewById(R.id.learn_button).isEnabled());
        click(R.id.learn_button);
        awaitPhase(LearningSessionViewModel.Phase.FRONT);
        assertEquals(vocabulary, model().snapshot().vocabulary);
        click(R.id.learning_reveal_button);
        click(R.id.learning_quality_3);
        awaitPhase(LearningSessionViewModel.Phase.FEEDBACK);
        await(() -> model().progress() != null);
        assertEquals(1, model().progress().correctCount);
        assertEquals("LEARNING", model().progress().status);
        LearningAttempt.Request original = model().snapshot().attempt;
        evidence.add("uiAttempt", JSON.toJsonTree(original));
        JsonObject first = accepted(original);
        assertTrue(first.get("isCorrect").getAsBoolean());
        assertEquals(3, first.get("answerQuality").getAsInt());
        assertEquals(model().feedback().attemptId, first.get("attemptId").getAsString());
        click(R.id.learning_complete_button);
        awaitPhase(LearningSessionViewModel.Phase.COMPLETED);
        assertEquals(1, model().summary().wordsStudiedCount);
        assertEquals(100, model().summary().accuracyPercent.intValueExact());
        evidence.addProperty("uiComplete", model().snapshot().completeEvent);
        assertEquals(first, accepted(original));
        var reused = new LearningAttempt.Request(original.sessionId, vocabulary, original.responseTimeMs, 2, original.eventId);
        error(app.api.createExplicitRequests(LearningAttemptApi.class).submit(reused).execute(), "IDEMPOTENCY_KEY_REUSE");
        evidence.add("uiResponse", first);
        checkpoint("UI");

        ui(() -> activity.getSupportFragmentManager().popBackStackImmediate());
        ui(() -> ((BottomNavigationView) activity.findViewById(R.id.main_navigation)).setSelectedItemId(R.id.nav_review));
        await(() -> activity.findViewById(R.id.review_rows) != null
                && ((android.view.ViewGroup) activity.findViewById(R.id.review_rows)).getChildCount() >= 2);
        ui(() -> ((android.view.ViewGroup) activity.findViewById(R.id.review_rows)).getChildAt(1).performClick());
        awaitPhase(LearningSessionViewModel.Phase.FRONT);
        assertEquals("REVIEW", model().snapshot().sessionType);
        click(R.id.learning_reveal_button); click(R.id.learning_quality_2);
        awaitPhase(LearningSessionViewModel.Phase.FEEDBACK);
        await(() -> model().progress() != null);
        assertFalse(model().feedback().isCorrect);
        assertEquals(1, model().progress().incorrectCount);
        evidence.add("reviewAttempt", JSON.toJsonTree(model().snapshot().attempt));
        click(R.id.learning_complete_button);
        awaitPhase(LearningSessionViewModel.Phase.COMPLETED);
        assertEquals(0, model().summary().accuracyPercent.intValueExact());
        evidence.addProperty("reviewComplete", model().snapshot().completeEvent);
        ui(() -> activity.getSupportFragmentManager().popBackStackImmediate());
        await(() -> activity.findViewById(R.id.review_status) != null
                && ((android.widget.TextView) activity.findViewById(R.id.review_status)).getText().toString()
                .equals(activity.getString(R.string.review_empty)));
        checkpoint("REVIEW");

        String duplicateSession = start();
        LearningAttempt.Request duplicate = request(duplicateSession, vocabulary);
        evidence.add("duplicate", JSON.toJsonTree(duplicate));
        checkpoint("DUPLICATE");
        List<Response<JsonElement>> duplicates = race(List.of(duplicate, duplicate, duplicate, duplicate));
        JsonElement snapshot = duplicates.get(0).body();
        for (Response<JsonElement> response : duplicates) {
            assertEquals(200, response.code()); assertEquals(snapshot, response.body());
        }
        evidence.add("duplicateResponse", snapshot);
        checkpoint("DUPLICATE_DONE");
        LearningAttempt.Request one = request(start(), vocabulary), two = request(start(), vocabulary);
        evidence.add("staleOne", JSON.toJsonTree(one)); evidence.add("staleTwo", JSON.toJsonTree(two));
        checkpoint("STALE");
        List<Response<JsonElement>> stale = race(List.of(one, two));
        assertTrue(stale.get(0).code() == 200 && stale.get(1).code() == 409
                || stale.get(1).code() == 200 && stale.get(0).code() == 409);
        int loser = stale.get(0).code() == 409 ? 0 : 1;
        error(stale.get(loser), "CONCURRENT_UPDATE");
        evidence.addProperty("staleLoser", loser);
        checkpoint("STALE_DONE");
        LearningAttempt.Request retry = loser == 0 ? one : two;
        JsonObject retried = accepted(retry);
        assertEquals(retried, accepted(retry));
        evidence.add("staleRetryResponse", retried);
        checkpoint("FINAL");
    }

    private LearningAttempt.Request request(String session, String vocabulary) {
        return new LearningAttempt.Request(session, vocabulary, 3000, 5, UUID.randomUUID().toString());
    }
    private String start() throws Exception {
        var response = app.api.createExplicitRequests(LearningSessionApi.class)
                .start(Map.of("sessionType", "MIXED", "eventId", UUID.randomUUID().toString())).execute();
        assertEquals(201, response.code());
        return response.body().getAsJsonObject().get("sessionId").getAsString();
    }
    private JsonObject accepted(LearningAttempt.Request request) throws Exception {
        Response<JsonElement> response = app.api.createExplicitRequests(LearningAttemptApi.class).submit(request).execute();
        assertEquals(200, response.code()); assertNotNull(response.body()); return response.body().getAsJsonObject();
    }
    private void error(Response<JsonElement> response, String code) {
        assertEquals(409, response.code());
        assertEquals(code, app.api.errors().parse(response).code);
    }
    private List<Response<JsonElement>> race(List<LearningAttempt.Request> requests) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(requests.size());
        CountDownLatch ready = new CountDownLatch(requests.size()), go = new CountDownLatch(1);
        try {
            List<Future<Response<JsonElement>>> futures = new ArrayList<>();
            for (LearningAttempt.Request request : requests) futures.add(pool.submit(() -> {
                ready.countDown(); assertTrue(go.await(10, TimeUnit.SECONDS));
                return app.api.createExplicitRequests(LearningAttemptApi.class).submit(request).execute();
            }));
            assertTrue(ready.await(10, TimeUnit.SECONDS)); go.countDown();
            List<Response<JsonElement>> result = new ArrayList<>();
            for (var future : futures) result.add(future.get(40, TimeUnit.SECONDS));
            return result;
        } finally { pool.shutdownNow(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS)); }
    }
    private void checkpoint(String stage) throws Exception {
        evidence.addProperty("stage", stage);
        Files.write(stageFile.toPath(), JSON.toJson(evidence).getBytes(StandardCharsets.UTF_8));
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(50);
        while (System.nanoTime() < end) {
            if (releaseFile.exists() && new String(Files.readAllBytes(releaseFile.toPath()), StandardCharsets.UTF_8).equals(stage)) return;
            Thread.sleep(100);
        }
        fail("Host checkpoint timeout: " + stage);
    }
    private LearningSessionViewModel model() {
        try {
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_content);
            Field field = fragment.getClass().getDeclaredField("model"); field.setAccessible(true);
            return (LearningSessionViewModel) field.get(fragment);
        } catch (Exception failure) { throw new AssertionError(failure); }
    }
    private void ui(Runnable action) {
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(action); instrumentation.waitForIdleSync();
    }
    private void click(int id) { ui(() -> { View view = activity.findViewById(id); assertNotNull(view); assertTrue(view.isEnabled()); view.performClick(); }); }
    private void awaitPhase(LearningSessionViewModel.Phase phase) throws Exception { await(() -> model().state().getValue().phase == phase); }
    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(35);
        while (System.nanoTime() < deadline) {
            boolean[] result = {false}; ui(() -> result[0] = condition.getAsBoolean());
            if (result[0]) return;
            Thread.sleep(100);
        }
        fail("UI condition timeout");
    }

    public static final class AnchorProvider extends Provider {
        public AnchorProvider() {
            super("EacE2eAnchor", 1.0, "QA-E2E-001 PKIX trust anchor trong test process");
            put("TrustManagerFactory.EacE2ePkix", AnchorFactory.class.getName());
        }
    }
    public static final class AnchorFactory extends TrustManagerFactorySpi {
        private TrustManager[] managers;
        @Override protected void engineInit(KeyStore ignored) throws KeyStoreException {
            try {
                KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType()); store.load(null);
                store.setCertificateEntry("qa-e2e-ephemeral", Objects.requireNonNull(anchor));
                TrustManagerFactory factory = TrustManagerFactory.getInstance("PKIX");
                factory.init(store); managers = factory.getTrustManagers();
            } catch (Exception failure) { throw new KeyStoreException(failure); }
        }
        @Override protected void engineInit(ManagerFactoryParameters parameters) throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException("Chỉ dùng ephemeral PKIX anchor");
        }
        @Override protected TrustManager[] engineGetTrustManagers() { return managers.clone(); }
    }
}
