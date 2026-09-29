package com.example.englishaicoach.vocabulary;

import com.example.englishaicoach.common.storage.ObjectStorageService;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Điều phối tạo audio cho từ vựng; GET metadata không phụ thuộc vào luồng này. */
@Service
public class TtsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TtsService.class);
    private final VocabularyRepository repository;
    private final TtsProvider provider;
    private final ObjectStorageService storage;

    public TtsService(VocabularyRepository repository, TtsProvider provider,
            ObjectStorageService storage) {
        this.repository = repository;
        this.provider = provider;
        this.storage = storage;
    }

    /** Tạo audio khi chưa có URL; lỗi dependency chỉ làm audio tạm vắng mặt. */
    public Optional<String> ensureAudio(UUID vocabularyId) {
        Optional<Vocabulary> found = repository.findActiveById(vocabularyId);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Vocabulary vocabulary = found.get();
        if (vocabulary.audioUrl() != null) {
            return Optional.of(vocabulary.audioUrl());
        }

        final String audioUrl;
        try {
            GeneratedAudio audio = provider.generateAudio(vocabulary.word());
            audioUrl = storage.store(audio.content(), audio.contentType());
            requirePublicHttpsUrl(audioUrl);
        } catch (RuntimeException failure) {
            // Không ghi nội dung từ, audio hoặc chi tiết provider vào log.
            LOGGER.warn("Không thể tạo/lưu audio từ vựng: {}", failure.getClass().getSimpleName());
            return Optional.empty();
        }

        repository.saveAudioUrlIfAbsent(vocabularyId, audioUrl);
        return repository.findActiveById(vocabularyId).map(Vocabulary::audioUrl);
    }

    private void requirePublicHttpsUrl(String audioUrl) {
        URI uri = URI.create(audioUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("URL audio phải là HTTPS công khai.");
        }
    }
}
