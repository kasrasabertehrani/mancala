package com.mancalagame.domain.event;


import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import lombok.Getter;


@Getter
public class PlayerJoinedEvent extends DomainEvent {

    private final PlayerId playerId;

    public PlayerJoinedEvent(RoomId roomId, PlayerId playerId) {
        super(roomId);
        this.playerId = playerId;
    }
}