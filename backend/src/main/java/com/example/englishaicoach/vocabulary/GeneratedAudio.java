package com.example.englishaicoach.vocabulary;

import java.util.Objects;

/** Kết quả audio nhị phân của TTS, không chứa URL hay thông tin nhà cung cấp. */
public record GeneratedAudio(byte[] content, String contentType) {

    public GeneratedAudio {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(contentType, "contentType");
        if (content.length == 0 || !contentType.startsWith("audio/")) {
            throw new IllegalArgumentException("Dữ liệu audio hoặc content type không hợp lệ.");
        }
        content = content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }

    @Override
    public String toString() {
        return "GeneratedAudio[content=<redacted>, contentType=" + contentType + "]";
    }
}
