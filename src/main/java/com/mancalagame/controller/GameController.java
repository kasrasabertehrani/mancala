package com.mancalagame.controller;

import com.mancalagame.model.GameRoom;
import com.mancalagame.payload.PlayPitCommand;
import com.mancalagame.service.GameService;
import com.mancalagame.service.SessionTracker;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionTracker sessionTracker; // Inject our new memory tracker!

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate, SessionTracker sessionTracker) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
        this.sessionTracker = sessionTracker;
    }

    @MessageMapping("/game.move")
    public void makeMove(@Payload PlayPitCommand command, SimpMessageHeaderAccessor headerAccessor) {

        // 1. Memorize this player's session ID in case they disconnect later!
        String sessionId = headerAccessor.getSessionId();
        sessionTracker.trackSession(sessionId, command.getPlayerId(), command.getRoomId());

        // 2. Make the move normally
        GameRoom updatedRoom = gameService.makeMove(
                command.getRoomId(),
                command.getPlayerId(),
                command.getPitIndex()
        );

        // 3. Broadcast the update
        messagingTemplate.convertAndSend("/topic/room/" + updatedRoom.getRoomId(), updatedRoom.getGame());
    }
}