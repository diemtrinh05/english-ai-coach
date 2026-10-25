package com.example.englishaicoach.assessment.dto;

import com.example.englishaicoach.assessment.AssessmentStatus;
import com.example.englishaicoach.assessment.AssessmentType;
import com.example.englishaicoach.assessment.CefrDifficulty;
import java.util.UUID;

public record AssessmentResponse(UUID assessmentId, AssessmentType type,
        AssessmentStatus status, CefrDifficulty currentDifficulty) { }
