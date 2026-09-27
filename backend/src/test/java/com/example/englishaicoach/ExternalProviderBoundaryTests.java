package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.englishaicoach.ai.AiGenerationRequest;
import com.example.englishaicoach.ai.AiGenerationResult;
import com.example.englishaicoach.ai.LlmProvider;
import com.example.englishaicoach.common.storage.ObjectStorageService;
import com.example.englishaicoach.notification.NotificationProvider;
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
        TtsProvider tts = text -> "audio: " + text;
        ObjectStorageService storage = (content, contentType) ->
                contentType + ":" + new String(content, StandardCharsets.UTF_8);
        AtomicBoolean pushSent = new AtomicBoolean();
        NotificationProvider notification = (pushToken, title, body) -> {
            if ("test-device".equals(pushToken)
                    && "Nhắc học".equals(title)
                    && "Đến giờ ôn tập".equals(body)) {
                pushSent.set(true);
            }
        };

        assertEquals("generated: prompt", llm.generate(new AiGenerationRequest("prompt")).content());
        assertEquals("audio: hello", tts.generateAudio("hello"));
        assertEquals("audio/mpeg:audio", storage.store(
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
