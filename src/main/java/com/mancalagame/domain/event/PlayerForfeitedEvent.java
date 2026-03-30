package com.mancalagame.domain.event;


import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;

public class PlayerForfeitedEvent extends DomainEvent {
    private final PlayerId playerId;
    private final String reason;

    public PlayerForfeitedEvent(RoomId roomId, PlayerId playerId, String reason) {
        super(roomId);
        this.playerId = playerId;
        this.reason = reason;
    }

    public PlayerId getPlayerId() { return playerId; }
    public String getReason() { return reason; }
}
