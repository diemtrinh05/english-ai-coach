package com.example.englishaicoach.core.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class AppViewModelTest {

    @Test
    public void initialStateIsReady() {
        AppViewModel viewModel = new AppViewModel();

        assertEquals(AppUiState.READY, viewModel.getUiState().getValue());
    }
}
