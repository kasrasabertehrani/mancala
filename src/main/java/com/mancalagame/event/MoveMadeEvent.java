package com.mancalagame.event;


import com.mancalagame.model.Game;

public class MoveMadeEvent extends DomainEvent {
    private final String playerId;
    private final int pitIndex;
    private final Game.GameStatus newStatus;

    public MoveMadeEvent(String roomId, String playerId, int pitIndex, Game.GameStatus newStatus) {
        super(roomId);
        this.playerId = playerId;
        this.pitIndex = pitIndex;
        this.newStatus = newStatus;
    }

    public String getPlayerId() { return playerId; }
    public int getPitIndex() { return pitIndex; }
    public Game.GameStatus getNewStatus() { return newStatus; }
}
