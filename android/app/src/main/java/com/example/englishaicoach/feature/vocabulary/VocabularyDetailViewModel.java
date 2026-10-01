package com.example.englishaicoach.feature.vocabulary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishaicoach.core.ui.UiState;
import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.repository.VocabularyRepository;
import com.example.englishaicoach.domain.usecase.BrowseVocabularyUseCase;

import java.util.List;

public final class VocabularyDetailViewModel extends ViewModel {
    private final BrowseVocabularyUseCase useCase;
    private final MutableLiveData<UiState<VocabularyItem>> detail =
            new MutableLiveData<>(UiState.initial());
    private final MutableLiveData<UiState<List<VocabularyItem.Example>>> examples =
            new MutableLiveData<>(UiState.initial());
    private VocabularyRepository.Cancelable detailCall;
    private VocabularyRepository.Cancelable examplesCall;
    private String id;
    private boolean online = true;
    private int generation;

    public VocabularyDetailViewModel(BrowseVocabularyUseCase useCase) { this.useCase = useCase; }
    public LiveData<UiState<VocabularyItem>> detail() { return detail; }
    public LiveData<UiState<List<VocabularyItem.Example>>> examples() { return examples; }
    public void setOnline(boolean online) { this.online = online; }

    public void load(String id) {
        this.id = id;
        if (detailCall != null) detailCall.cancel();
        if (examplesCall != null) examplesCall.cancel();
        int current = ++generation;
        detail.setValue(UiState.loading());
        examples.setValue(UiState.loading());
        detailCall = useCase.detail(id, online, new VocabularyRepository.Callback<VocabularyItem>() {
            @Override public void onSuccess(VocabularyItem item, boolean fromCache) {
                if (current == generation) detail.setValue(UiState.success(item));
            }
            @Override public void onFailure(VocabularyRepository.Failure failure) {
                if (current == generation) detail.setValue(failure == VocabularyRepository.Failure.OFFLINE
                        ? UiState.offline() : UiState.error());
            }
        });
        examplesCall = useCase.examples(id, online,
                new VocabularyRepository.Callback<List<VocabularyItem.Example>>() {
                    @Override public void onSuccess(List<VocabularyItem.Example> value,
                                                    boolean fromCache) {
                        if (current != generation) return;
                        examples.setValue(value.isEmpty() ? UiState.empty() : UiState.success(value));
                    }
                    @Override public void onFailure(VocabularyRepository.Failure failure) {
                        if (current == generation) examples.setValue(
                                failure == VocabularyRepository.Failure.OFFLINE
                                        ? UiState.offline() : UiState.error());
                    }
                });
    }

    public void retry() { if (id != null) load(id); }

    @Override protected void onCleared() {
        if (detailCall != null) detailCall.cancel();
        if (examplesCall != null) examplesCall.cancel();
        super.onCleared();
    }
}
