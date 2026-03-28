package com.mancalagame.infrastructure.adapter.in.websocket;

import com.mancalagame.infrastructure.adapter.in.websocket.payload.PlayPitCommand;
import com.mancalagame.infrastructure.adapter.in.websocket.payload.ReconnectRequest;
import com.mancalagame.application.service.GameService;
import com.mancalagame.infrastructure.SessionTracker;
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
    public void makeMove(@Payload PlayPitCommand command, SimpMessageHeaderAccessor headerAccessor) {
        if (command.getRoomId() == null || command.getPlayerId() == null) {
            throw new IllegalArgumentException("Room ID and Player ID are required.");
        }

        // 1. Memorize this player's session ID (acts as a safety net)
        sessionTracker.trackSession(headerAccessor.getSessionId(), command.getPlayerId(), command.getRoomId());

        // 2. Tell the Service to make the move.
        // Notice: We deleted the manual broadcast!
        // The GameService will generate a MoveMadeEvent, and our GameEventBroadcaster will catch it.
        gameService.makeMove(command.getRoomId(), command.getPlayerId(), command.getPitIndex());
    }

    @MessageMapping("/game.reconnect")
    public void reconnect(@Payload ReconnectRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        // 1. Re-register their brand new WebSocket session in our memory
        sessionTracker.trackSession(sessionId, request.getPlayerId(), request.getRoomId());

        // 2. Tell the Domain to stop the forfeit timer and update its internal session mapping.
        // The GameEventBroadcaster will automatically catch the PlayerReconnectedEvent!
        gameService.handlePlayerReconnect(request.getRoomId(), request.getPlayerId(), sessionId);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception ex) {
        return ex.getMessage();
    }
}