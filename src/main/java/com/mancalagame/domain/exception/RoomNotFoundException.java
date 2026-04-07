package com.mancalagame.domain.exception;


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