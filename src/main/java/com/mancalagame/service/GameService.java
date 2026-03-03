package com.mancalagame.service;

import com.mancalagame.model.Game;
import com.mancalagame.model.GameRoom;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final RoomService roomService;

    public GameService(RoomService roomService) {
        this.roomService = roomService;
    }

    public GameRoom makeMove(String roomId, String playerId, int pitIndex) {
        // 1. Find the room
        GameRoom room = roomService.getRoom(roomId);

        // 2. Tell the Game to execute the move!
        room.getGame().playTurn(playerId, pitIndex);

        // 3. Return the updated room
        return room;
    }

    // Add this to GameService.java
    public GameRoom handlePlayerDisconnect(String roomId, String playerId) {
        GameRoom room = roomService.getRoom(roomId);

        if (room != null) {
            Game game = room.getGame();
            // The Service asks the Referee to handle it, but only if the game isn't already over!
            if (game.getGameStatus() != Game.GameStatus.GAME_OVER) {
                game.handleDisconnect(playerId);
            }
        }
        return room;
    }
}