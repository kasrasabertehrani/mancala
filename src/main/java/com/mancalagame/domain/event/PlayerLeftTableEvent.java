package com.mancalagame.domain.event;


import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;

public class PlayerLeftTableEvent extends DomainEvent {
    private final PlayerId playerId;

    public PlayerLeftTableEvent(RoomId roomId, PlayerId playerId) {
        super(roomId);
        this.playerId = playerId;
    }

    public PlayerId getPlayerId() { return playerId; }
}
