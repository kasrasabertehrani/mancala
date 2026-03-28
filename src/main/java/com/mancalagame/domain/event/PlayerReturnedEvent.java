package com.mancalagame.domain.event;


public class PlayerReturnedEvent extends DomainEvent {
    private final String playerId;

    public PlayerReturnedEvent(String roomId, String playerId) {
        super(roomId);
        this.playerId = playerId;
    }

    public String getPlayerId() { return playerId; }
}
