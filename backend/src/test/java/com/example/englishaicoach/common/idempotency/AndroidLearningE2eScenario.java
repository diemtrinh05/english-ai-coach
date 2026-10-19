package com.example.englishaicoach.common.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

import com.example.englishaicoach.learning.LearningAttemptRepository;
import com.example.englishaicoach.learning.UserVocabularyProgress;
import com.example.englishaicoach.support.PostgreSqlTestContainerConfiguration;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.security.KeyStore;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** QA-E2E-001: explicit local gate; tên Scenario không nằm trong default Surefire scan. */
@EnabledIfSystemProperty(named = "eac.e2e.serial", matches = "\\S+")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.hibernate.ddl-auto=validate")
@Import({PostgreSqlTestContainerConfiguration.class, AndroidLearningE2eScenario.TimeConfiguration.class})
class AndroidLearningE2eScenario {
    private static final Instant NOW = Instant.parse("2026-10-18T08:00:00Z");
    private static final String PASSWORD = randomSecret();
    private static final String JWT_SECRET = randomSecret();
    // Chỉ tạo TLS khi explicit runner đã qua điều kiện serial; discovery không chạy process ngoài.
    private static final class TlsFixture {
        private static final Path DIRECTORY = createTls();
    }
    private static final String APP = "com.example.englishaicoach.debug";
    private static final String CLASS = "com.example.englishaicoach.e2e.RealBackendLearningInstrumentedTest";
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @MockitoSpyBean IdempotencyRepository claims;
    @MockitoSpyBean LearningAttemptRepository attempts;
    private final Path repository = Path.of(System.getProperty("user.dir")).toAbsolutePath().getParent();
    private final String serial = System.getProperty("eac.e2e.serial");
    private final String run = UUID.randomUUID().toString();
    private final UUID firstWord = UUID.randomUUID(), dueWord = UUID.randomUUID();
    private UUID user;
    private Process device;
    private Path output;
    private final List<Boolean> inserted = new CopyOnWriteArrayList<>();
    private final List<Long> loadedVersions = new CopyOnWriteArrayList<>();

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("server.ssl.enabled", () -> true);
        registry.add("server.ssl.key-store", () -> TlsFixture.DIRECTORY.resolve("server.p12").toUri().toString());
        registry.add("server.ssl.key-store-password", () -> PASSWORD);
        registry.add("server.ssl.key-store-type", () -> "PKCS12");
        registry.add("server.ssl.key-alias", () -> "fixture");
        registry.add("app.jwt.secret", () -> JWT_SECRET);
        registry.add("app.jwt.refresh-token-expiration", () -> "7d");
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "SET statement_timeout = '20s'");
    }

    @Test void realAndroidVerticalSliceHasAtomicServerEffectsAndDeterministicRaces() throws Exception {
        String word = "qae2e" + run.replace("-", "").substring(0, 12);
        fixtureWord(firstWord, word); fixtureWord(dueWord, "review" + word);
        output = Files.createTempDirectory("qa-e2e-001-" + run + "-");
        try {
            command(repository.resolve("android"), output.resolve("build.log"), 300,
                    "cmd.exe", "/d", "/c", "gradlew.bat", "--no-daemon", "--max-workers=2",
                    "-Dorg.gradle.jvmargs=-Xmx768m -Dfile.encoding=UTF-8", "assembleDebug", "assembleDebugAndroidTest",
                    "-PeacApiBaseUrl=https://127.0.0.1:" + port + "/api/v1/");
            assertThat(adb("install", "-r", repository.resolve("android/app/build/outputs/apk/debug/app-debug.apk").toString())).contains("Success");
            assertThat(adb("install", "-r", repository.resolve("android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk").toString())).contains("Success");
            adb("reverse", "tcp:" + port, "tcp:" + port);
            KeyStore store = KeyStore.getInstance("PKCS12");
            try (InputStream input = Files.newInputStream(TlsFixture.DIRECTORY.resolve("server.p12"))) { store.load(input, PASSWORD.toCharArray()); }
            String ca = Base64.getEncoder().encodeToString(store.getCertificate("fixture").getEncoded());
            device = new ProcessBuilder(adbArgs("shell", "am", "instrument", "-w", "-r", "-e", "class", CLASS,
                    "-e", "e2eRun", run, "-e", "e2eCa", ca, "-e", "e2eWord", word,
                    "-e", "e2eVocabulary", firstWord.toString(), APP + ".test/androidx.test.runner.AndroidJUnitRunner"))
                    .redirectErrorStream(true).redirectOutput(output.resolve("device.log").toFile()).start();
            List<String> stages = List.of("FIXTURE", "UI", "REVIEW", "DUPLICATE", "DUPLICATE_DONE", "STALE", "STALE_DONE", "FINAL");
            for (String stage : stages) {
                JsonNode evidence = awaitStage(stage);
                if (user == null) user = UUID.fromString(evidence.get("user").asString());
                switch (stage) {
                    case "FIXTURE" -> {
                        assertThat(jdbc.queryForObject("SELECT email FROM users WHERE id=?", String.class, user))
                                .isEqualTo("e2e-" + run + "@example.test");
                        jdbc.update("""
                                INSERT INTO user_vocabulary_progress
                                (id,user_id,vocabulary_id,status,ease_factor,interval_days,repetitions,next_review_at,
                                 last_reviewed_at,correct_count,incorrect_count,last_quality,version,created_at,updated_at)
                                VALUES (?,?,?,'LEARNING',2.50,1,1,?,?,1,0,5,0,?,?)
                                """, UUID.randomUUID(), user, dueWord, ts(NOW.minusSeconds(1)), ts(NOW.minusSeconds(86401)), ts(NOW), ts(NOW));
                        assertThat(count("session_attempts")).isZero();
                    }
                    case "UI" -> {
                        assertThat(count("session_attempts")).isEqualTo(1);
                        assertThat(xp()).isEqualTo(25);
                        assertAttempt(evidence.get("uiAttempt"), evidence.get("uiResponse"));
                        assertSrs(firstWord, "LEARNING", "2.36", 1, 1, 1, 0);
                        assertThat(jdbc.queryForObject("SELECT status FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?", String.class, user, firstWord)).isEqualTo("LEARNING");
                        assertCompletion(evidence.get("uiAttempt").get("sessionId").asString(), "MIXED", "100.00");
                    }
                    case "REVIEW" -> {
                        assertThat(count("session_attempts")).isEqualTo(2);
                        assertThat(xp()).isEqualTo(35);
                        JsonNode request = evidence.get("reviewAttempt");
                        assertThat(request.get("vocabularyId").asString()).isEqualTo(dueWord.toString());
                        assertThat(jdbc.queryForObject("SELECT incorrect_count FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?", Integer.class, user, dueWord)).isEqualTo(1);
                        assertSrs(dueWord, "LEARNING", "2.18", 1, 0, 1, 1);
                        assertCompletion(request.get("sessionId").asString(), "REVIEW", "0.00");
                    }
                    case "DUPLICATE" -> {
                        CyclicBarrier contenders = new CyclicBarrier(4);
                        UUID event = UUID.fromString(evidence.get("duplicate").get("eventId").asString());
                        doAnswer(invocation -> {
                            contenders.await(15, TimeUnit.SECONDS);
                            boolean value = (boolean) invocation.callRealMethod(); inserted.add(value); return value;
                        }).when(claims).claim(org.mockito.ArgumentMatchers.eq(event), org.mockito.ArgumentMatchers.eq(user),
                                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
                    }
                    case "DUPLICATE_DONE" -> {
                        reset(claims);
                        assertThat(inserted).containsExactlyInAnyOrder(true, false, false, false);
                        assertThat(count("session_attempts")).isEqualTo(3);
                        assertThat(xp()).isEqualTo(40);
                        assertAttempt(evidence.get("duplicate"), evidence.get("duplicateResponse"));
                    }
                    case "STALE" -> {
                        CyclicBarrier loaded = new CyclicBarrier(2);
                        doAnswer(invocation -> {
                            UserVocabularyProgress value = (UserVocabularyProgress) invocation.callRealMethod();
                            loadedVersions.add(value.getVersion()); loaded.await(15, TimeUnit.SECONDS); return value;
                        }).when(attempts).loadOrInitialize(user, firstWord, NOW);
                    }
                    case "STALE_DONE" -> {
                        reset(attempts);
                        assertThat(loadedVersions).hasSize(2);
                        assertThat(loadedVersions.get(0)).isEqualTo(loadedVersions.get(1));
                        JsonNode loser = evidence.get(evidence.get("staleLoser").asInt() == 0 ? "staleOne" : "staleTwo");
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM idempotency_keys WHERE event_id=?", Integer.class,
                                UUID.fromString(loser.get("eventId").asString()))).isZero();
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM session_attempts WHERE session_id=?", Integer.class,
                                UUID.fromString(loser.get("sessionId").asString()))).isZero();
                        assertThat(jdbc.queryForObject("SELECT words_studied_count FROM learning_sessions WHERE id=?", Integer.class,
                                UUID.fromString(loser.get("sessionId").asString()))).isZero();
                        assertThat(count("session_attempts")).isEqualTo(4); assertThat(xp()).isEqualTo(45);
                        assertThat(jdbc.queryForObject("SELECT correct_count FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?", Integer.class, user, firstWord)).isEqualTo(3);
                    }
                    case "FINAL" -> {
                        assertThat(count("session_attempts")).isEqualTo(5);
                        assertThat(count("learning_sessions")).isEqualTo(5);
                        assertThat(count("idempotency_keys")).isEqualTo(12);
                        assertThat(count("xp_logs")).isEqualTo(7); assertThat(xp()).isEqualTo(50);
                        assertThat(count("streaks")).isEqualTo(1);
                        assertSrs(firstWord, "REVIEWING", "2.66", 40, 4, 4, 0);
                        assertThat(jdbc.queryForObject("SELECT current_streak FROM streaks WHERE user_id=?", Integer.class, user)).isEqualTo(1);
                        assertThat(jdbc.queryForObject("SELECT correct_count FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?", Integer.class, user, firstWord)).isEqualTo(4);
                        assertThat(jdbc.queryForObject("SELECT version FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?", Long.class, user, firstWord)).isEqualTo(4);
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=? AND reason='LEARNING_CORRECT'", Integer.class, user)).isEqualTo(4);
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=? AND reason='SESSION_COMPLETED'", Integer.class, user)).isEqualTo(2);
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs WHERE user_id=? AND reason='STREAK_MAINTAINED'", Integer.class, user)).isEqualTo(1);
                        JsonNode loser = evidence.get(evidence.get("staleLoser").asInt() == 0 ? "staleOne" : "staleTwo");
                        assertAttempt(loser, evidence.get("staleRetryResponse"));
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs x LEFT JOIN session_attempts a ON a.id=x.source_event_id WHERE x.user_id=? AND x.reason='LEARNING_CORRECT' AND a.id IS NULL", Integer.class, user)).isZero();
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs x JOIN learning_sessions s ON s.id=x.source_event_id WHERE x.user_id=? AND x.reason='SESSION_COMPLETED' AND x.reference_type='learning_sessions' AND x.reference_id=s.id AND x.xp_amount=10 AND s.ended_at IS NOT NULL", Integer.class, user)).isEqualTo(2);
                        UUID streakEvent = UUID.nameUUIDFromBytes(("gamification-v1:STREAK_MAINTAINED:" + user + ":2026-10-18").getBytes(StandardCharsets.UTF_8));
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM xp_logs x JOIN streaks s ON s.id=x.reference_id WHERE x.user_id=? AND x.reason='STREAK_MAINTAINED' AND x.reference_type='streaks' AND x.source_event_id=? AND x.xp_amount=10", Integer.class, user, streakEvent)).isEqualTo(1);
                        Files.writeString(output.resolve("evidence.json"), evidence.toString(), StandardCharsets.UTF_8);
                    }
                    default -> throw new AssertionError(stage);
                }
                release(stage);
                System.out.println("QA-E2E-001 checkpoint " + stage + " PASS run=" + run);
            }
            assertThat(device.waitFor(45, TimeUnit.SECONDS)).isTrue();
            assertThat(device.exitValue()).isZero();
            String deviceLog = Files.readString(output.resolve("device.log"));
            assertThat(deviceLog).contains("OK (1 test)").doesNotContain("FAILURES!!!", "INSTRUMENTATION_FAILED",
                    "AssumptionFailure", "AssumptionViolatedException", "INSTRUMENTATION_STATUS_CODE: -4");
            System.out.println("QA-E2E-001 real TLS/Android/PostgreSQL PASS; artifacts=" + output);
        } finally {
            reset(claims, attempts);
            if (device != null && device.isAlive()) { device.destroyForcibly(); device.waitFor(10, TimeUnit.SECONDS); }
            adb("shell", "am", "force-stop", APP);
            adb("reverse", "--remove", "tcp:" + port);
            if (user == null) {
                var found = jdbc.queryForList("SELECT id FROM users WHERE email=?", UUID.class, "e2e-" + run + "@example.test");
                if (!found.isEmpty()) user = found.getFirst();
            }
            cleanupOwned();
            // Các path đều là file cố định trong TEMP riêng do chính test tạo; không recursive delete.
            Files.deleteIfExists(TlsFixture.DIRECTORY.resolve("server.p12")); Files.deleteIfExists(TlsFixture.DIRECTORY.resolve("keytool.log")); Files.deleteIfExists(TlsFixture.DIRECTORY);
        }
    }

    private void fixtureWord(UUID id, String word) {
        jdbc.update("""
                INSERT INTO vocabulary(id,word,meaning_vi,meaning_en,part_of_speech,cefr_level_id,source,is_active,created_at,updated_at)
                VALUES (?,?,'từ kiểm thử','test word','NOUN',(SELECT id FROM cefr_levels WHERE code='A1'),'MANUAL',true,?,?)
                """, id, word, ts(NOW), ts(NOW));
        jdbc.update("INSERT INTO vocabulary_examples(id,vocabulary_id,example_text,translation_text,source,created_at) VALUES (?,?,?,'Ví dụ kiểm thử','MANUAL',?)",
                UUID.randomUUID(), id, "This is " + word + ".", ts(NOW));
    }
    private void assertAttempt(JsonNode request, JsonNode response) {
        UUID event = UUID.fromString(request.get("eventId").asString());
        assertThat(json.readTree(jdbc.queryForObject("SELECT response_snapshot::text FROM idempotency_keys WHERE event_id=?", String.class, event))).isEqualTo(response);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM session_attempts WHERE id=? AND session_id=? AND vocabulary_id=? AND answer_quality=? AND attempted_at=?", Integer.class,
                UUID.fromString(response.get("attemptId").asString()), UUID.fromString(request.get("sessionId").asString()),
                UUID.fromString(request.get("vocabularyId").asString()), request.get("answerQuality").asInt(), ts(NOW))).isEqualTo(1);
        JsonNode progress = response.get("progress");
        assertThat(jdbc.queryForObject("SELECT next_review_at FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?", Timestamp.class,
                user, UUID.fromString(request.get("vocabularyId").asString())).toInstant()).isEqualTo(Instant.parse(progress.get("nextReviewAt").asString()));
    }
    private void assertCompletion(String session, String type, String accuracy) {
        var row = jdbc.queryForMap("SELECT session_type,ended_at,started_at,words_studied_count,accuracy_percent FROM learning_sessions WHERE id=? AND user_id=?", UUID.fromString(session), user);
        assertThat(row.get("session_type")).isEqualTo(type);
        assertThat(((Timestamp) row.get("ended_at")).toInstant()).isEqualTo(NOW);
        assertThat(((Timestamp) row.get("started_at")).toInstant()).isEqualTo(NOW);
        assertThat(row.get("words_studied_count")).isEqualTo(1);
        assertThat(row.get("accuracy_percent").toString()).isEqualTo(accuracy);
    }
    private void assertSrs(UUID vocabulary, String status, String ease, int interval, int repetitions, int correct, int incorrect) {
        var row = jdbc.queryForMap("SELECT status,ease_factor,interval_days,repetitions,correct_count,incorrect_count,next_review_at,last_reviewed_at FROM user_vocabulary_progress WHERE user_id=? AND vocabulary_id=?", user, vocabulary);
        assertThat(row.get("status")).isEqualTo(status);
        assertThat(row.get("ease_factor").toString()).isEqualTo(ease);
        assertThat(row.get("interval_days")).isEqualTo(interval);
        assertThat(row.get("repetitions")).isEqualTo(repetitions);
        assertThat(row.get("correct_count")).isEqualTo(correct);
        assertThat(row.get("incorrect_count")).isEqualTo(incorrect);
        assertThat(((Timestamp) row.get("last_reviewed_at")).toInstant()).isEqualTo(NOW);
        assertThat(((Timestamp) row.get("next_review_at")).toInstant()).isEqualTo(NOW.plusSeconds(86400L * interval));
    }
    private int count(String table) {
        return table.equals("session_attempts")
                ? jdbc.queryForObject("SELECT count(*) FROM session_attempts WHERE session_id IN (SELECT id FROM learning_sessions WHERE user_id=?)", Integer.class, user)
                : jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE user_id=?", Integer.class, user);
    }
    private int xp() { return jdbc.queryForObject("SELECT COALESCE(SUM(xp_amount),0) FROM xp_logs WHERE user_id=?", Integer.class, user); }
    private JsonNode awaitStage(String expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(70);
        while (System.nanoTime() < deadline) {
            String body = adb("shell", "run-as", APP, "cat", "cache/qa-e2e-" + run + ".json");
            try {
                JsonNode current = json.readTree(body);
                if (current.has("stage") && expected.equals(current.get("stage").asString())) return current;
            } catch (RuntimeException notReady) { /* File có thể chưa được ghi xong. */ }
            if (!device.isAlive()) throw new AssertionError("Android kết thúc trước " + expected + "; xem " + output.resolve("device.log"));
            Thread.sleep(200);
        }
        throw new AssertionError("Checkpoint timeout " + expected + "; xem " + output.resolve("device.log"));
    }
    private void release(String stage) throws Exception {
        adb("shell", "run-as", APP, "sh", "-c", "'printf " + stage + " > cache/qa-e2e-" + run + ".release'");
    }
    private String adb(String... args) throws Exception {
        Process process = new ProcessBuilder(adbArgs(args)).redirectErrorStream(true).start();
        assertThat(process.waitFor(20, TimeUnit.SECONDS)).as("adb bounded timeout").isTrue();
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
    }
    private List<String> adbArgs(String... args) {
        String sdk = Optional.ofNullable(System.getenv("ANDROID_HOME")).orElse(System.getProperty("user.home") + "/AppData/Local/Android/Sdk");
        List<String> command = new ArrayList<>(List.of(Path.of(sdk, "platform-tools/adb.exe").toString(), "-s", serial));
        command.addAll(List.of(args)); return command;
    }
    private static void command(Path cwd, Path log, int seconds, String... args) throws Exception {
        Process process = new ProcessBuilder(args).directory(cwd.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new AssertionError("Command timeout; log=" + log); }
        assertThat(process.exitValue()).as("Command log=" + log).isZero();
    }
    private void cleanupOwned() {
        if (user != null) {
            jdbc.update("DELETE FROM session_attempts WHERE session_id IN (SELECT id FROM learning_sessions WHERE user_id=?)", user);
            for (String table : List.of("idempotency_keys", "xp_logs", "streaks", "user_vocabulary_progress", "learning_sessions", "user_profiles", "refresh_tokens"))
                jdbc.update("DELETE FROM " + table + " WHERE user_id=?", user);
            jdbc.update("DELETE FROM users WHERE id=?", user);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE id=?", Integer.class, user)).isZero();
        }
        for (UUID word : List.of(firstWord, dueWord)) {
            jdbc.update("DELETE FROM vocabulary_examples WHERE vocabulary_id=?", word);
            jdbc.update("DELETE FROM vocabulary WHERE id=?", word);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM vocabulary WHERE id=?", Integer.class, word)).isZero();
        }
    }
    private static Timestamp ts(Instant instant) { return Timestamp.from(instant); }
    private static String randomSecret() { byte[] bytes = new byte[48]; new SecureRandom().nextBytes(bytes); return Base64.getEncoder().encodeToString(bytes); }
    private static Path createTls() {
        try {
            Path directory = Files.createTempDirectory("qa-e2e-001-tls-");
            ProcessBuilder generator = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin/keytool.exe").toString(),
                    "-genkeypair", "-alias", "fixture", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
                    "-dname", "CN=QA-E2E-001", "-ext", "BC=ca:true", "-ext", "SAN=ip:127.0.0.1,dns:localhost",
                    "-storetype", "PKCS12", "-keystore", directory.resolve("server.p12").toString(),
                    "-storepass:env", "EAC_E2E_KEYSTORE_PASSWORD", "-keypass:env", "EAC_E2E_KEYSTORE_PASSWORD", "-noprompt");
            generator.environment().put("EAC_E2E_KEYSTORE_PASSWORD", PASSWORD);
            Process process = generator.redirectErrorStream(true).redirectOutput(directory.resolve("keytool.log").toFile()).start();
            if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() != 0) throw new AssertionError("Không tạo được TLS fixture");
            return directory;
        } catch (Exception failure) { throw new ExceptionInInitializerError(failure); }
    }
    @TestConfiguration(proxyBeanMethods = false) static class TimeConfiguration {
        @Bean @Primary Clock e2eClock() { return Clock.fixed(NOW, ZoneOffset.UTC); }
    }
}
