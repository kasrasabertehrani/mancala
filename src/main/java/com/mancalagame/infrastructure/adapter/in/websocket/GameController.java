package com.mancalagame.infrastructure.adapter.in.websocket;

import com.mancalagame.application.service.GameService;
import com.mancalagame.domain.exception.DomainException;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import com.mancalagame.infrastructure.SessionTracker;
import com.mancalagame.infrastructure.adapter.in.websocket.payload.PlayPitCommand;
import com.mancalagame.infrastructure.adapter.in.websocket.payload.ReconnectRequest;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    private final GameService gameService;
    private final SessionTracker sessionTracker;

    public GameController(GameService gameService, SessionTracker sessionTracker) {
        this.gameService = gameService;
        this.sessionTracker = sessionTracker;
    }

    @MessageMapping("/game.move")
    public void makeMove(@Valid @Payload PlayPitCommand command, SimpMessageHeaderAccessor headerAccessor) {
        // 1. Validate Session
        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null) {
            throw new IllegalArgumentException("Invalid session");
        }

        // 2. Translate to Value Objects
        RoomId roomId = new RoomId(command.getRoomId());
        PlayerId playerId = new PlayerId(command.getPlayerId());

        // 3. Track and Execute
        sessionTracker.trackSession(sessionId, command.getPlayerId(), command.getRoomId());
        gameService.makeMove(roomId, playerId, command.getPitIndex());
    }

    @MessageMapping("/game.reconnect")
    public void reconnect(@Valid @Payload ReconnectRequest request, SimpMessageHeaderAccessor headerAccessor) {
        // 1. Validate Session
        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null) {
            throw new IllegalArgumentException("Invalid session");
        }

        // 2. Translate to Value Objects
        RoomId roomId = new RoomId(request.getRoomId());
        PlayerId playerId = new PlayerId(request.getPlayerId());

        // 3. Track and Execute
        sessionTracker.trackSession(sessionId, request.getPlayerId(), request.getRoomId());
        gameService.handlePlayerReconnect(roomId, playerId);
    }

    // --- EXCEPTION HANDLERS ---

    // Catches Domain Exceptions (like InvalidGameState) and sends the error to the specific user
    @MessageExceptionHandler({DomainException.class, IllegalArgumentException.class})
    @SendToUser("/queue/errors")
    public String handleException(Exception ex) {
        return ex.getMessage();
    }

    // Catches the @Valid payload validation failures and extracts your custom error messages
    @MessageExceptionHandler(org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public String handleValidationExceptions(org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException ex) {
        if (ex.getBindingResult().hasErrors()) {
            return ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        return "Invalid request format";
    }
}