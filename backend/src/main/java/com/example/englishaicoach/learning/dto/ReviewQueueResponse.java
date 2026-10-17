package com.example.englishaicoach.learning.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewQueueResponse(List<Item> items, int count) {
    public record Item(UUID vocabularyId, String word, String status, Instant nextReviewAt) {
    }
}
