package com.mancalagame.service;

import com.mancalagame.model.Game;
import com.mancalagame.model.GameRoom;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class GameService {

    private final RoomService roomService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public GameService(RoomService roomService) {
        this.roomService = roomService;
    }

    public GameRoom makeMove(String roomId, String playerId, int pitIndex) {
        GameRoom room = roomService.getRoom(roomId);
        room.getGame().playTurn(playerId, pitIndex);

        if (room.getGame().getGameStatus() == Game.GameStatus.GAME_OVER) {
            scheduleRoomCleanup(roomId);
        }

        return room;
    }

    public GameRoom handlePlayerDisconnect(String roomId, String playerId) {
        GameRoom room = roomService.getRoom(roomId);

        if (room != null) {
            Game game = room.getGame();
            if (game.getGameStatus() != Game.GameStatus.GAME_OVER) {
                game.handleDisconnect(playerId);
            }
            scheduleRoomCleanup(roomId);
        }
        return room;
    }

    private void scheduleRoomCleanup(String roomId) {
        scheduler.schedule(() -> roomService.removeRoom(roomId), 5, TimeUnit.SECONDS);
    }
}
