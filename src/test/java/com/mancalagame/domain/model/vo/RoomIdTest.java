package com.mancalagame.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RoomIdTest {

    @Test
    void shouldThrowExceptionWhenRoomIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new RoomId(null));
    }

    @Test
    void shouldThrowExceptionWhenRoomIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new RoomId(" "));
    }

    @Test
    void shouldCreateRoomIdWhenValueIsValid() {
        RoomId roomId = new RoomId("room1");
        assertEquals("room1", roomId.value());
    }
}
