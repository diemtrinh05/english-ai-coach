package com.example.englishaicoach.vocabulary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.englishaicoach.common.storage.ObjectStorageService;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class TtsConcurrencyIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private static final String FIRST_URL = "https://media.example.test/first.mp3";
    private static final String SECOND_URL = "https://media.example.test/second.mp3";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private VocabularyRepository repository;

    @Test
    void concurrentGenerationKeepsFirstPersistedUrlForBothCallers() throws Exception {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO vocabulary
                (id, word, meaning_vi, cefr_level_id, source, is_active, created_at, updated_at)
                VALUES (?, ?, 'từ thử nghiệm',
                    (SELECT id FROM cefr_levels WHERE code = 'A1'),
                    'MANUAL', TRUE, NOW(), NOW())
                """, id, "tts-concurrency-" + id);

        CyclicBarrier bothReadNull = new CyclicBarrier(2);
        CountDownLatch releaseSecondStorage = new CountDownLatch(1);
        AtomicInteger generated = new AtomicInteger();
        AtomicInteger stored = new AtomicInteger();
        TtsProvider provider = text -> {
            generated.incrementAndGet();
            try {
                // Cả hai caller đã đọc audio_url = NULL trước khi tạo media.
                bothReadNull.await(10, TimeUnit.SECONDS);
            } catch (Exception failure) {
                throw new IllegalStateException("Không đồng bộ được hai lượt TTS.", failure);
            }
            byte marker = (byte) (Thread.currentThread().getName().equals("tts-first") ? 1 : 2);
            return new GeneratedAudio(new byte[] {marker}, "audio/mpeg");
        };
        ObjectStorageService storage = (content, contentType) -> {
            stored.incrementAndGet();
            if (content[0] == 1) {
                return FIRST_URL;
            }
            try {
                // Giữ caller thứ hai sau khi caller đầu đã ghi URL vào PostgreSQL.
                if (!releaseSecondStorage.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Hết thời gian chờ URL đầu tiên.");
                }
            } catch (InterruptedException failure) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Bị ngắt khi chờ URL đầu tiên.", failure);
            }
            return SECOND_URL;
        };
        TtsService service = new TtsService(repository, provider, storage);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(() -> {
                Thread.currentThread().setName("tts-first");
                return service.ensureAudio(id).orElse(null);
            });
            Future<String> second = executor.submit(() -> {
                Thread.currentThread().setName("tts-second");
                return service.ensureAudio(id).orElse(null);
            });

            assertEquals(FIRST_URL, first.get(20, TimeUnit.SECONDS));
            assertEquals(FIRST_URL, persistedUrl(id));
            releaseSecondStorage.countDown();
            assertEquals(FIRST_URL, second.get(20, TimeUnit.SECONDS));
            assertEquals(FIRST_URL, persistedUrl(id));
            assertEquals(2, generated.get());
            assertEquals(2, stored.get());
        } finally {
            releaseSecondStorage.countDown();
            executor.shutdownNow();
            jdbc.update("DELETE FROM vocabulary WHERE id = ?", id);
        }
    }

    private String persistedUrl(UUID id) {
        return jdbc.queryForObject("SELECT audio_url FROM vocabulary WHERE id = ?", String.class, id);
    }
}
