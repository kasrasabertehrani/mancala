package com.mancalagame.domain.model.vo;

import com.fasterxml.jackson.annotation.JsonValue;

public record RoomId(@JsonValue String value) {
    public RoomId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
    }
}