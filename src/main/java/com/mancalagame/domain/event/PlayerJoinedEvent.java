package com.mancalagame.domain.event;


public class PlayerJoinedEvent extends DomainEvent {
    private final String playerId;

    public PlayerJoinedEvent(String roomId, String playerId) {
        super(roomId);
        this.playerId = playerId;
    }

    public String getPlayerId() { return playerId; }
}
