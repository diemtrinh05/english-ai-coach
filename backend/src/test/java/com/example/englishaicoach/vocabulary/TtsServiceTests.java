package com.example.englishaicoach.vocabulary;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.englishaicoach.common.storage.ObjectStorageService;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class TtsServiceTests {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000123");
    private static final String URL = "https://media.example.test/vocabulary/audio.mp3";

    @Test
    void storesGeneratedBytesAndReturnsSavedUrl() {
        VocabularyRepository repository = mock(VocabularyRepository.class);
        when(repository.findActiveById(ID)).thenReturn(Optional.of(word(null)), Optional.of(word(URL)));
        byte[] content = {1, 2, 3};
        TtsProvider provider = text -> {
            assertEquals("hello", text);
            return new GeneratedAudio(content, "audio/mpeg");
        };
        ObjectStorageService storage = (bytes, contentType) -> {
            assertArrayEquals(content, bytes);
            assertEquals("audio/mpeg", contentType);
            return URL;
        };

        assertEquals(Optional.of(URL), new TtsService(repository, provider, storage).ensureAudio(ID));
        verify(repository).saveAudioUrlIfAbsent(ID, URL);
    }

    @Test
    void cachedAudioSkipsBothDependencies() {
        VocabularyRepository repository = mock(VocabularyRepository.class);
        when(repository.findActiveById(ID)).thenReturn(Optional.of(word(URL)));
        AtomicBoolean called = new AtomicBoolean();
        TtsProvider provider = text -> {
            called.set(true);
            throw new IllegalStateException();
        };
        ObjectStorageService storage = (bytes, contentType) -> {
            called.set(true);
            throw new IllegalStateException();
        };

        assertEquals(Optional.of(URL), new TtsService(repository, provider, storage).ensureAudio(ID));
        assertFalse(called.get());
        verify(repository, never()).saveAudioUrlIfAbsent(ID, URL);
    }

    @Test
    void providerAndStorageFailureLeaveMetadataAvailable() {
        VocabularyRepository repository = mock(VocabularyRepository.class);
        when(repository.findActiveById(ID)).thenReturn(Optional.of(word(null)));
        TtsProvider failedProvider = text -> {
            throw new IllegalStateException("provider unavailable");
        };
        ObjectStorageService failedStorage = (bytes, contentType) -> {
            throw new IllegalStateException("storage unavailable");
        };
        TtsProvider provider = text -> new GeneratedAudio(new byte[] {1}, "audio/mpeg");

        assertTrue(new TtsService(repository, failedProvider, failedStorage)
                .ensureAudio(ID).isEmpty());
        assertTrue(new TtsService(repository, provider, failedStorage).ensureAudio(ID).isEmpty());
        assertEquals("hello", repository.findActiveById(ID).orElseThrow().word());
    }

    @Test
    void invalidStorageUrlIsNeverPersisted() {
        VocabularyRepository repository = mock(VocabularyRepository.class);
        when(repository.findActiveById(ID)).thenReturn(Optional.of(word(null)));
        TtsProvider provider = text -> new GeneratedAudio(new byte[] {1}, "audio/mpeg");
        ObjectStorageService storage = (bytes, contentType) -> "http://media.example.test/audio.mp3";

        assertTrue(new TtsService(repository, provider, storage).ensureAudio(ID).isEmpty());
        verify(repository, never()).saveAudioUrlIfAbsent(ID, "http://media.example.test/audio.mp3");
    }

    @Test
    void generatedAudioProtectsBinaryContent() {
        byte[] original = "secret payload".getBytes(StandardCharsets.UTF_8);
        GeneratedAudio audio = new GeneratedAudio(original, "audio/mpeg");
        original[0] = 0;
        byte[] exposed = audio.content();
        exposed[0] = 0;
        assertEquals('s', audio.content()[0]);
        assertFalse(audio.toString().contains("secret payload"));
    }

    private Vocabulary word(String audioUrl) {
        return new Vocabulary(ID, "hello", null, "xin chào", null, null,
                "A1", audioUrl, null);
    }
}
