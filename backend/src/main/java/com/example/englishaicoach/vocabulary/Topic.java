package com.example.englishaicoach.vocabulary;

import java.util.UUID;

public record Topic(UUID id, String name, String description, String iconUrl,
        UUID parentTopicId, boolean isActive) {
}
