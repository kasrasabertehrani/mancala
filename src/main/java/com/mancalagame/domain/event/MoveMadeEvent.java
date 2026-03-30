package com.mancalagame.domain.event;


import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;

public class MoveMadeEvent extends DomainEvent {
    private final PlayerId playerId;
    private final int pitIndex;
    private final Game.GameStatus newStatus;

    public MoveMadeEvent(RoomId roomId, PlayerId playerId, int pitIndex, Game.GameStatus newStatus) {
        super(roomId);
        this.playerId = playerId;
        this.pitIndex = pitIndex;
        this.newStatus = newStatus;
    }

    public PlayerId getPlayerId() { return playerId; }
    public int getPitIndex() { return pitIndex; }
    public Game.GameStatus getNewStatus() { return newStatus; }
}
