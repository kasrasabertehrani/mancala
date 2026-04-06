package com.mancalagame.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerIdTest {

    @Test
    void shouldThrowExceptionWhenPlayerIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerId(null));
    }

    @Test
    void shouldThrowExceptionWhenPlayerIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerId(" "));
    }

    @Test
    void shouldCreatePlayerIdWhenValueIsValid() {
        PlayerId playerId = new PlayerId("player1");
        assertEquals("player1", playerId.value());
    }
}
