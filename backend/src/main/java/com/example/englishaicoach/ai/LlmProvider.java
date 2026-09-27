package com.example.englishaicoach.ai;

/** Ranh giới nội bộ cho lệnh sinh nội dung AI đồng bộ. */
public interface LlmProvider {

    AiGenerationResult generate(AiGenerationRequest request);
}
