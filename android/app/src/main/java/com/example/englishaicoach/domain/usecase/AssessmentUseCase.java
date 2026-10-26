package com.example.englishaicoach.domain.usecase;

import com.example.englishaicoach.domain.model.Assessment;
import com.example.englishaicoach.domain.repository.AssessmentRepository;

public final class AssessmentUseCase {
    private final AssessmentRepository repository;
    public AssessmentUseCase(AssessmentRepository repository) { this.repository = repository; }
    public AssessmentRepository.Cancelable start(String owner, String event, AssessmentRepository.Callback<String> callback) {
        return repository.start(owner, new Assessment.Start(event), callback);
    }
    public AssessmentRepository.Cancelable result(String owner, String id, AssessmentRepository.Callback<Assessment.Result> callback) {
        return repository.result(owner, id, callback);
    }
    public AssessmentRepository.Cancelable question(String owner, String id, AssessmentRepository.Callback<Assessment.Question> callback) {
        return repository.question(owner, id, callback);
    }
    public AssessmentRepository.Cancelable answer(String owner, String id, Assessment.Answer request, AssessmentRepository.Callback<Assessment.Ack> callback) {
        return repository.answer(owner, id, request, callback);
    }
}
