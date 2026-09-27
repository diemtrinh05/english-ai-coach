package com.example.englishaicoach.vocabulary;

/** Ranh giới nội bộ cho việc tạo audio, tách khỏi SDK TTS. */
public interface TtsProvider {

    String generateAudio(String text);
}
