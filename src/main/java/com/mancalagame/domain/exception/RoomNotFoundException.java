package com.mancalagame.domain.exception;

/**
 * Thrown when a GameRoom cannot be found by its ID.
 */
public class RoomNotFoundException extends DomainException {

    private final String roomId;

    public RoomNotFoundException(String roomId) {
        super("Game room not found: " + roomId);
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}