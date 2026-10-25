package com.example.englishaicoach.assessment.dto;

import com.example.englishaicoach.assessment.AssessmentType;
import java.util.UUID;

public record StartAssessmentRequest(AssessmentType type, UUID eventId) { }
