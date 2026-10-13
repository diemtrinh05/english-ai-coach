package com.example.englishaicoach.learning.srs;

import com.example.englishaicoach.config.SrsProperties;
import com.example.englishaicoach.learning.LearningStatus;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Điều phối một phép tính sm2-ext-v1 và chuyển trạng thái học theo baseline. */
@Service
public final class SrsService {

    private final SrsCalculator calculator;
    private final SrsProperties properties;

    public SrsService(SrsProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.calculator = new SrsCalculator(properties);
    }

    public SrsTransitionResult calculate(LearningStatus oldStatus, SrsCalculationInput input) {
        Objects.requireNonNull(oldStatus, "oldStatus");
        SrsCalculationResult calculation = calculator.calculate(Objects.requireNonNull(input, "input"));
        int quality = input.answerQuality();
        LearningStatus nextStatus = switch (oldStatus) {
            // NEW chỉ có trước attempt đầu tiên được chấp nhận, kể cả khi q < 3.
            case NEW -> LearningStatus.LEARNING;
            case LEARNING -> calculation.isCorrect() && calculation.newRepetitions() >= 2
                    ? LearningStatus.REVIEWING : LearningStatus.LEARNING;
            case REVIEWING -> {
                if (!calculation.isCorrect()) {
                    yield LearningStatus.LEARNING;
                }
                boolean mastered = quality >= properties.masteredMinimumQuality()
                        && calculation.newRepetitions() >= properties.masteredMinimumRepetitions()
                        && calculation.newIntervalDays() >= properties.masteredMinimumIntervalDays();
                yield mastered ? LearningStatus.MASTERED : LearningStatus.REVIEWING;
            }
            case MASTERED -> calculation.isCorrect() ? LearningStatus.MASTERED : LearningStatus.LEARNING;
        };
        return new SrsTransitionResult(calculation, nextStatus);
    }
}
