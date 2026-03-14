package com.mancalagame.domain.event;


public class PlayerReconnectedEvent extends DomainEvent {
    private final String playerId;

    public PlayerReconnectedEvent(String roomId, String playerId) {
        super(roomId);
        this.playerId = playerId;
    }

    public String getPlayerId() { return playerId; }
}
