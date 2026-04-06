package com.mancalagame.domain.model.vo;

import com.fasterxml.jackson.annotation.JsonValue;


public record PlayerId(@JsonValue String value) {

    public PlayerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Player ID cannot be null or blank");
        }
    }
}