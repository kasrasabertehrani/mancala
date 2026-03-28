package com.mancalagame.domain.event;


public class PlayerLeftTableEvent extends DomainEvent {
    private final String playerId;

    public PlayerLeftTableEvent(String roomId, String playerId) {
        super(roomId);
        this.playerId = playerId;
    }

    public String getPlayerId() { return playerId; }
}
