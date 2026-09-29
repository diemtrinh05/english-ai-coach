package com.example.englishaicoach.vocabulary;

/** Adapter disabled mặc định cho môi trường chưa cấu hình nhà cung cấp TTS. */
public final class UnavailableTtsProvider implements TtsProvider {

    @Override
    public GeneratedAudio generateAudio(String text) {
        throw new IllegalStateException("TTS chưa được cấu hình.");
    }
}
