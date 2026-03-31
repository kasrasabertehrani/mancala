package com.mancalagame.infrastructure.adapter.in.websocket.payload;

import jakarta.validation.constraints.NotBlank;

public class ReconnectRequest {

    @NotBlank(message = "Room ID is required and cannot be empty")
    private String roomId;

    @NotBlank(message = "Room ID is required and cannot be empty")
    private String playerId;

    public ReconnectRequest() {}

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
}