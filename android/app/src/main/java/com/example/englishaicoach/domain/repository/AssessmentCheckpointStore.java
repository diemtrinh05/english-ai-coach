package com.example.englishaicoach.domain.repository;

import com.example.englishaicoach.domain.model.Assessment;

public interface AssessmentCheckpointStore {
    Assessment.Checkpoint read();
    void write(Assessment.Checkpoint checkpoint);
}
