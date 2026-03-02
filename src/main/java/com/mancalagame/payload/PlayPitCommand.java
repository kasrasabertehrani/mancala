package com.mancalagame.payload;

public class PlayPitCommand {
    private String roomId;
    private String playerId;
    private int pitIndex; // Which pit they clicked (0-13)

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