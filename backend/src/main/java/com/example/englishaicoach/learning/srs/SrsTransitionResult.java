package com.example.englishaicoach.learning.srs;

import com.example.englishaicoach.learning.LearningStatus;
import java.util.Objects;

/** Kết quả tính SRS và trạng thái học mới của một attempt đã được chấp nhận. */
public record SrsTransitionResult(SrsCalculationResult calculation, LearningStatus status) {

    public SrsTransitionResult {
        Objects.requireNonNull(calculation, "calculation");
        Objects.requireNonNull(status, "status");
    }
}
