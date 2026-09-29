package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.englishaicoach.ai.AiGenerationRequest;
import com.example.englishaicoach.ai.AiGenerationResult;
import com.example.englishaicoach.ai.LlmProvider;
import com.example.englishaicoach.common.storage.ObjectStorageService;
import com.example.englishaicoach.notification.NotificationProvider;
import com.example.englishaicoach.vocabulary.GeneratedAudio;
import com.example.englishaicoach.vocabulary.TtsProvider;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class ExternalProviderBoundaryTests {

    @Test
    void providerPortsAreInterfaces() {
        assertTrue(LlmProvider.class.isInterface());
        assertTrue(TtsProvider.class.isInterface());
        assertTrue(ObjectStorageService.class.isInterface());
        assertTrue(NotificationProvider.class.isInterface());
    }

    @Test
    void providerPortsSupportSdkFreeFakeAdapters() {
        LlmProvider llm = request -> new AiGenerationResult("generated: " + request.prompt());
        TtsProvider tts = text -> new GeneratedAudio(
                ("audio: " + text).getBytes(StandardCharsets.UTF_8), "audio/mpeg");
        ObjectStorageService storage = (content, contentType) -> {
            assertEquals("audio", new String(content, StandardCharsets.UTF_8));
            assertEquals("audio/mpeg", contentType);
            return "https://media.example.test/audio.mp3";
        };
        AtomicBoolean pushSent = new AtomicBoolean();
        NotificationProvider notification = (pushToken, title, body) -> {
            if ("test-device".equals(pushToken)
                    && "Nhắc học".equals(title)
                    && "Đến giờ ôn tập".equals(body)) {
                pushSent.set(true);
            }
        };

        assertEquals("generated: prompt", llm.generate(new AiGenerationRequest("prompt")).content());
        assertEquals("audio: hello", new String(tts.generateAudio("hello").content(),
                StandardCharsets.UTF_8));
        assertEquals("https://media.example.test/audio.mp3", storage.store(
                "audio".getBytes(StandardCharsets.UTF_8), "audio/mpeg"));
        notification.send("test-device", "Nhắc học", "Đến giờ ôn tập");
        assertTrue(pushSent.get());
    }

    @Test
    void aiPayloadDoesNotLeakThroughDefaultLoggingRepresentation() {
        String sensitivePrompt = "personal prompt content";
        String sensitiveOutput = "personal generated content";

        assertFalse(new AiGenerationRequest(sensitivePrompt).toString().contains(sensitivePrompt));
        assertFalse(new AiGenerationResult(sensitiveOutput).toString().contains(sensitiveOutput));
    }
}
