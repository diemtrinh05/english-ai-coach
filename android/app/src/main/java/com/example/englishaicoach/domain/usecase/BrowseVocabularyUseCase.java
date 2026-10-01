package com.example.englishaicoach.domain.usecase;

import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.repository.VocabularyRepository;

import java.util.List;

public final class BrowseVocabularyUseCase {
    private final VocabularyRepository repository;
    public BrowseVocabularyUseCase(VocabularyRepository repository) { this.repository = repository; }

    public VocabularyRepository.Cancelable list(VocabularyRepository.Query query, boolean online,
                                                VocabularyRepository.Callback<VocabularyRepository.Page> callback) {
        return repository.list(query, online, callback);
    }

    public VocabularyRepository.Cancelable detail(String id, boolean online,
                                                  VocabularyRepository.Callback<VocabularyItem> callback) {
        return repository.detail(id, online, callback);
    }

    public VocabularyRepository.Cancelable examples(String id, boolean online,
            VocabularyRepository.Callback<List<VocabularyItem.Example>> callback) {
        return repository.examples(id, online, callback);
    }

    public VocabularyRepository.Cancelable topics(boolean online,
            VocabularyRepository.Callback<List<VocabularyItem.Topic>> callback) {
        return repository.topics(online, callback);
    }
}
