package com.mancalagame.event;

import java.time.Instant;

public abstract class DomainEvent {
    private final String roomId;
    private final Instant occurredOn;

    public DomainEvent(String roomId) {
        this.roomId = roomId;
        this.occurredOn = Instant.now();
    }

    public String getRoomId() { return roomId; }
    public Instant getOccurredOn() { return occurredOn; }
}