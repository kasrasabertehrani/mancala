package com.mancalagame.domain.exception;

/**
 * Base class for all domain-level exceptions.
 * These represent violations of business rules and invariants.
 */
public abstract class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}