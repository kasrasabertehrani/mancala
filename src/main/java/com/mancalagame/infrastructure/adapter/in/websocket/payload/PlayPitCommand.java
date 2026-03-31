package com.mancalagame.infrastructure.adapter.in.websocket.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlayPitCommand {

    @NotBlank(message = "Room ID is required and cannot be empty")
    private String roomId;

    @NotBlank(message = "Player ID is required and cannot be empty")
    private String playerId;

    @NotNull(message = "Pit index is required")
    @Min(value = 0, message = "Pit index cannot be less than 0")
    @Max(value = 13, message = "Pit index cannot be greater than 13")
    private Integer pitIndex; // Which pit they clicked (0-13)

    // Add empty constructor for Spring JSON parsing
    public PlayPitCommand() {}

    // Add Getters and Setters for all three!
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getPitIndex() {
        return pitIndex;
    }

    public void setPitIndex(int pitIndex) {
        this.pitIndex = pitIndex;
    }
}