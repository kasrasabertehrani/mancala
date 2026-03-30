package com.mancalagame.domain.event;

import com.mancalagame.domain.model.vo.RoomId;

import java.time.Instant;

public abstract class DomainEvent {
    private final RoomId roomId;
    private final Instant occurredOn;

    public DomainEvent(RoomId roomId) {
        this.roomId = roomId;
        this.occurredOn = Instant.now();
    }

    public RoomId getRoomId() { return roomId; }
    public Instant getOccurredOn() { return occurredOn; }
}