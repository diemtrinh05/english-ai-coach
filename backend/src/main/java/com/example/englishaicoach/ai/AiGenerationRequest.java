package com.example.englishaicoach.ai;

/** Dữ liệu đầu vào độc lập với SDK của nhà cung cấp LLM. */
public record AiGenerationRequest(String prompt) {

    @Override
    public String toString() {
        return "AiGenerationRequest[redacted]";
    }
}
