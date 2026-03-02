package com.mancalagame.controller;

import com.mancalagame.model.GameRoom;
import com.mancalagame.payload.PlayPitCommand;
import com.mancalagame.service.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    // Spring injects your GameService and the WebSocket broadcaster
    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    // Listens for messages sent to "/app/game.move"
    @MessageMapping("/game.move")
    public void makeMove(@Payload PlayPitCommand command) {

        // 1. Tell your awesome GameService to make the move
        GameRoom updatedRoom = gameService.makeMove(
                command.getRoomId(),
                command.getPlayerId(),
                command.getPitIndex()
        );

        // 2. Broadcast the fresh game state to EVERYONE sitting in this specific room!
        // Anyone subscribed to "/topic/room/1" will instantly receive this JSON.
        messagingTemplate.convertAndSend("/topic/room/" + updatedRoom.getRoomId(), updatedRoom.getGame());
    }
}