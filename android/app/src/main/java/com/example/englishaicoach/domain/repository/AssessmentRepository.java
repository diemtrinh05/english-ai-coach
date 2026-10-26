package com.example.englishaicoach.domain.repository;

import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.domain.model.Assessment;

public interface AssessmentRepository {
    interface Cancelable { void cancel(); }
    interface Callback<T> { void completed(T value, ApiError error); }
    Cancelable start(String owner, Assessment.Start request, Callback<String> callback);
    Cancelable result(String owner, String id, Callback<Assessment.Result> callback);
    Cancelable question(String owner, String id, Callback<Assessment.Question> callback);
    Cancelable answer(String owner, String id, Assessment.Answer request, Callback<Assessment.Ack> callback);
}
