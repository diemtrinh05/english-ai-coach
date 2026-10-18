package com.example.englishaicoach.domain.model;
public final class ReviewItem {
    public final String vocabularyId, word, status, nextReviewAt;
    public ReviewItem(String id, String word, String status, String next) {
        this.vocabularyId = id; this.word = word; this.status = status; this.nextReviewAt = next;
    }
}
