package com.mancalagame.domain.event;


import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import lombok.Getter;


@Getter
public class PlayerReturnedEvent extends DomainEvent {
    private final PlayerId playerId;

    public PlayerReturnedEvent(RoomId roomId, PlayerId playerId) {
        super(roomId);
        this.playerId = playerId;
    }
}