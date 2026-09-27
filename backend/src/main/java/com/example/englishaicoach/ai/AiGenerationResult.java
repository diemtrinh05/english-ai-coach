package com.example.englishaicoach.ai;

/** Nội dung thô do nhà cung cấp LLM trả về trước bước kiểm tra nghiệp vụ. */
public record AiGenerationResult(String content) {

    @Override
    public String toString() {
        return "AiGenerationResult[redacted]";
    }
}
