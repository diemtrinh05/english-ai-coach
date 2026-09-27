package com.example.englishaicoach.core.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public final class UiStateTest {
    @Test
    public void networkStatesAreExclusiveAndOnlySuccessCarriesData() {
        assertEquals(UiState.Status.INITIAL, UiState.initial().getStatus());
        assertEquals(UiState.Status.LOADING, UiState.loading().getStatus());
        assertEquals(UiState.Status.EMPTY, UiState.empty().getStatus());
        assertEquals(UiState.Status.ERROR, UiState.error().getStatus());
        assertEquals(UiState.Status.OFFLINE, UiState.offline().getStatus());
        assertNull(UiState.offline().getData());
        UiState<String> success = UiState.success("dữ liệu");
        assertEquals(UiState.Status.SUCCESS, success.getStatus());
        assertEquals("dữ liệu", success.getData());
    }

    @Test
    public void successRejectsNullPayload() {
        try {
            UiState.success(null);
            fail("Success không được chứa dữ liệu null.");
        } catch (NullPointerException expected) {
            // Thành công phải có dữ liệu để tránh trạng thái mơ hồ với Empty.
        }
    }
}
