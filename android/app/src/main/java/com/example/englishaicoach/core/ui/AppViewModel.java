package com.example.englishaicoach.core.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public final class AppViewModel extends ViewModel {

    private final MutableLiveData<AppUiState> uiState =
            new MutableLiveData<>(AppUiState.READY);

    public LiveData<AppUiState> getUiState() {
        return uiState;
    }
}
