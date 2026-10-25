package com.example.englishaicoach.assessment.dto;

import com.example.englishaicoach.assessment.CefrDifficulty;

public record AssessmentAnswerResponse(boolean correct, CefrDifficulty nextDifficulty,
        int questionsAnswered, boolean isCompleted) { }
