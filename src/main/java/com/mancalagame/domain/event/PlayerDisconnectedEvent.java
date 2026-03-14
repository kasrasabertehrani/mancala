package com.mancalagame.domain.event;


public class PlayerDisconnectedEvent extends DomainEvent {
    private final String playerId;

    public PlayerDisconnectedEvent(String roomId, String playerId) {
        super(roomId);
        this.playerId = playerId;
    }

    public String getPlayerId() { return playerId; }
}
