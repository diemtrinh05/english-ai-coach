package com.example.englishaicoach.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Các ngưỡng điều khiển placement assessment theo approved baseline
 * {@code assessment-block-v1}.
 *
 * <p>Các giá trị đếm dùng đơn vị câu hỏi hoặc block. {@code minimumQuestions} không được
 * lớn hơn {@code maximumQuestions}; các ngưỡng promote/demote được hiểu trong phạm vi
 * {@code blockSize}; {@code optionsPerQuestion} là số lựa chọn của mỗi câu hỏi. Thay đổi
 * các default này là thay đổi baseline, không phải tùy chỉnh vận hành thông thường.</p>
 */
@ConfigurationProperties("app.assessment")
public record AssessmentProperties(
        String algorithmVersion,
        int minimumQuestions,
        int maximumQuestions,
        int blockSize,
        int promoteCorrectThreshold,
        int demoteCorrectThreshold,
        int stableHoldBlocksRequired,
        String startingLevel,
        int optionsPerQuestion,
        int minimumVocabularyPerLevel) {
}
