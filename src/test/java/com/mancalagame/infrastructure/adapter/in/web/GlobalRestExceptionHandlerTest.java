package com.mancalagame.infrastructure.adapter.in.web;

import com.mancalagame.domain.exception.InvalidGameStateException;
import com.mancalagame.domain.exception.RoomNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalRestExceptionHandlerTest {

    private final GlobalRestExceptionHandler handler = new GlobalRestExceptionHandler();

    @Test
    void handleRoomNotFound_shouldReturn404AndErrorMessage() {
        RoomNotFoundException ex = new RoomNotFoundException("room-1");

        ResponseEntity<String> response = handler.handleRoomNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Error: Game room not found: room-1", response.getBody());
    }

    @Test
    void handleDomainException_shouldReturn400AndErrorMessage() {
        InvalidGameStateException ex = new InvalidGameStateException("Room is full");

        ResponseEntity<String> response = handler.handleDomainException(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Error: Room is full", response.getBody());
    }

    @Test
    void handleValidationExceptions_shouldReturn400AndFirstValidationMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        ObjectError error = new ObjectError("createRoomRequest", "Player name is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        ResponseEntity<String> response = handler.handleValidationExceptions(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid Request: Player name is required", response.getBody());
    }
}

