package com.mancalagame.domain.event;


public class PlayerForfeitedEvent extends DomainEvent {
    private final String playerId;
    private final String reason;

    public PlayerForfeitedEvent(String roomId, String playerId, String reason) {
        super(roomId);
        this.playerId = playerId;
        this.reason = reason;
    }

    public String getPlayerId() { return playerId; }
    public String getReason() { return reason; }
}
