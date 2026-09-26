package com.example.englishaicoach.core.network;

import java.util.UUID;

public final class LogicalEvent {
    private final UUID eventId;

    private LogicalEvent(UUID eventId) {
        this.eventId = eventId;
    }

    public static LogicalEvent begin() {
        return new LogicalEvent(UUID.randomUUID());
    }

    public String eventId() {
        return eventId.toString();
    }

    // Mỗi lần retry phải dùng lại cùng đối tượng và cùng eventId trong request body.
    public LogicalEvent retry() {
        return this;
    }
}
