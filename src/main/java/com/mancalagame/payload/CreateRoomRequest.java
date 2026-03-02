package com.mancalagame.payload;

public class CreateRoomRequest {
    private String playerName;

    public CreateRoomRequest() {}

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}