package com.example.englishaicoach.core.auth;

import androidx.annotation.Nullable;

public interface TokenStore {
    @Nullable TokenSession read();
    void write(@Nullable TokenSession session);
}
