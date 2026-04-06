package com.mancalagame.domain.event;

import com.mancalagame.domain.model.vo.RoomId;
import lombok.Getter;

import java.time.Instant;


@Getter
public abstract class DomainEvent {

    private final RoomId roomId;
    private final Instant occurredOn;

    public DomainEvent(RoomId roomId) {
        this.roomId = roomId;
        this.occurredOn = Instant.now();
    }
}