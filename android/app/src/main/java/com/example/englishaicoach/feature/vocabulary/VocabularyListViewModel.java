package com.example.englishaicoach.feature.vocabulary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishaicoach.core.ui.UiState;
import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.repository.VocabularyRepository;
import com.example.englishaicoach.domain.usecase.BrowseVocabularyUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VocabularyListViewModel extends ViewModel {
    private static final int PAGE_SIZE = 20;
    private final BrowseVocabularyUseCase useCase;
    private final MutableLiveData<UiState<List<VocabularyItem>>> state =
            new MutableLiveData<>(UiState.initial());
    private final MutableLiveData<Boolean> loadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> nextPageFailed = new MutableLiveData<>(false);
    private final MutableLiveData<List<VocabularyItem.Topic>> topics = new MutableLiveData<>();
    private final List<VocabularyItem> items = new ArrayList<>();
    private VocabularyRepository.Cancelable active;
    private VocabularyRepository.Cancelable topicCall;
    private VocabularyRepository.Query query = new VocabularyRepository.Query(
            0, PAGE_SIZE, null, null, null, null);
    private int requestGeneration;
    private int nextPage;
    private boolean hasNext;
    private boolean online = true;

    public VocabularyListViewModel(BrowseVocabularyUseCase useCase) { this.useCase = useCase; }
    public LiveData<UiState<List<VocabularyItem>>> state() { return state; }
    public LiveData<Boolean> loadingMore() { return loadingMore; }
    public LiveData<Boolean> nextPageFailed() { return nextPageFailed; }
    public boolean hasNext() { return hasNext; }
    public VocabularyRepository.Query currentQuery() { return query; }
    public LiveData<List<VocabularyItem.Topic>> topics() { return topics; }

    public void loadTopics() {
        if (topicCall != null) topicCall.cancel();
        topicCall = useCase.topics(online,
                new VocabularyRepository.Callback<List<VocabularyItem.Topic>>() {
                    @Override public void onSuccess(List<VocabularyItem.Topic> value,
                                                    boolean fromCache) { topics.setValue(value); }
                    @Override public void onFailure(VocabularyRepository.Failure failure) {
                        // Bộ lọc chủ đề chưa khả dụng; danh sách từ vẫn dùng được.
                    }
                });
    }

    public void setOnline(boolean online) {
        this.online = online;
        if (topics.getValue() == null) loadTopics();
        if (!online && items.isEmpty()) search(query.search, query.cefr, query.topicId,
                query.partOfSpeech);
    }

    public void search(String search, String cefr, String topicId, String partOfSpeech) {
        if (active != null) active.cancel();
        requestGeneration++;
        query = new VocabularyRepository.Query(0, PAGE_SIZE, search, cefr, topicId,
                partOfSpeech);
        items.clear();
        nextPage = 0;
        hasNext = false;
        nextPageFailed.setValue(false);
        state.setValue(UiState.loading());
        load(false);
    }

    public void retry() {
        if (items.isEmpty()) search(query.search, query.cefr, query.topicId,
                query.partOfSpeech);
        else if (hasNext) loadNext();
    }

    public void loadNext() {
        if (!hasNext || Boolean.TRUE.equals(loadingMore.getValue())) return;
        loadingMore.setValue(true);
        nextPageFailed.setValue(false);
        load(true);
    }

    private void load(boolean append) {
        int generation = requestGeneration;
        VocabularyRepository.Query pageQuery = new VocabularyRepository.Query(nextPage,
                PAGE_SIZE, query.search, query.cefr, query.topicId, query.partOfSpeech);
        active = useCase.list(pageQuery, online, new VocabularyRepository.Callback<VocabularyRepository.Page>() {
            @Override public void onSuccess(VocabularyRepository.Page page, boolean fromCache) {
                if (generation != requestGeneration) return;
                if (append) items.addAll(page.items);
                else { items.clear(); items.addAll(page.items); }
                nextPage = page.page + 1;
                hasNext = page.hasNext;
                loadingMore.setValue(false);
                nextPageFailed.setValue(false);
                if (items.isEmpty()) state.setValue(UiState.empty());
                else state.setValue(UiState.success(Collections.unmodifiableList(
                        new ArrayList<>(items))));
            }
            @Override public void onFailure(VocabularyRepository.Failure failure) {
                if (generation != requestGeneration) return;
                loadingMore.setValue(false);
                if (append) nextPageFailed.setValue(true);
                else state.setValue(failure == VocabularyRepository.Failure.OFFLINE
                        ? UiState.offline() : UiState.error());
            }
        });
    }

    @Override protected void onCleared() {
        if (active != null) active.cancel();
        if (topicCall != null) topicCall.cancel();
        super.onCleared();
    }
}
