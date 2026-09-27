package com.example.englishaicoach.core.navigation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public final class MainNavigationViewModel extends ViewModel {
    private final MutableLiveData<MainDestination> destination =
            new MutableLiveData<>(MainDestination.HOME);

    public LiveData<MainDestination> getDestination() {
        return destination;
    }

    public void select(MainDestination selected) {
        destination.setValue(selected);
    }
}
