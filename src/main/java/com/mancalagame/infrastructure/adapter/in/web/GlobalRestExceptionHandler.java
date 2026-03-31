package com.mancalagame.infrastructure.adapter.in.web;

import com.mancalagame.domain.exception.DomainException;
import com.mancalagame.domain.exception.RoomNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    // Automatically catches RoomNotFoundException and returns a 404 Not Found
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<String> handleRoomNotFound(RoomNotFoundException ex) {
        return ResponseEntity.status(404).body("Error: " + ex.getMessage());
    }

    // Automatically catches any other DomainException (like InvalidGameStateException)
    // and returns a 400 Bad Request
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<String> handleDomainException(DomainException ex) {
        return ResponseEntity.status(400).body("Error: " + ex.getMessage());
    }

    // Make sure to import org.springframework.web.bind.MethodArgumentNotValidException!
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        // This extracts the first error message (e.g., "Player name must be between 1 and 50 characters")
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(400).body("Invalid Request: " + errorMessage);
    }
}
