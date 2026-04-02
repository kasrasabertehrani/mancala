package com.mancalagame.domain.event;


import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;

public class MoveMadeEvent extends DomainEvent {
    private final PlayerId playerId;
    private final int pitIndex;
    private final Game.GameStatus newStatus;
    private final boolean captureOccurred;    // ← NEW
    private final boolean freeTurnGranted;

    public MoveMadeEvent(RoomId roomId, PlayerId playerId, int pitIndex, Game.GameStatus newStatus
            , boolean captureOccurred, boolean freeTurnGranted) {
        super(roomId);
        this.playerId = playerId;
        this.pitIndex = pitIndex;
        this.newStatus = newStatus;
        this.captureOccurred = captureOccurred;
        this.freeTurnGranted = freeTurnGranted;
    }

    public PlayerId getPlayerId() { return playerId; }
    public int getPitIndex() { return pitIndex; }
    public Game.GameStatus getNewStatus() { return newStatus; }
    public boolean isCaptureOccurred() { return captureOccurred; }     // ← NEW
    public boolean isFreeTurnGranted() { return freeTurnGranted; }
}
