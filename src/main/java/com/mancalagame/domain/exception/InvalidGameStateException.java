package com.mancalagame.domain.exception;

/**
 * Thrown when an operation cannot be performed due to invalid game state.
 */
public class InvalidGameStateException extends DomainException {

    public InvalidGameStateException(String message) {
        super(message);
    }
}