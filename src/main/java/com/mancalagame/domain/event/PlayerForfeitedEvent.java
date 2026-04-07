package com.mancalagame.domain.event;


import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import lombok.Getter;


@Getter
public class PlayerForfeitedEvent extends DomainEvent {

    private final PlayerId playerId;
    private final String reason;

    public PlayerForfeitedEvent(RoomId roomId, PlayerId playerId, String reason) {
        super(roomId);
        this.playerId = playerId;
        this.reason = reason;
    }
}