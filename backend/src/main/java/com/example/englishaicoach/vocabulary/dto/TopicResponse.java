package com.example.englishaicoach.vocabulary.dto;

import java.util.UUID;

public record TopicResponse(UUID id, String name, String description, String iconUrl,
        UUID parentTopicId, boolean isActive) {
}
