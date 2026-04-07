package com.mancalagame.domain.exception;


public class InvalidPlayerException extends DomainException {

    private final String playerId;

    public InvalidPlayerException(String playerId, String message) {
        super(message + " - Player: " + playerId);
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }
}