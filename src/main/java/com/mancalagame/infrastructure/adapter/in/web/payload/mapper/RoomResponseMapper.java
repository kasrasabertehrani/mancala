package com.mancalagame.infrastructure.adapter.in.web.payload.mapper;

import com.mancalagame.domain.model.Board;
import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.Pit;
import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.Room;
import com.mancalagame.infrastructure.adapter.in.web.payload.response.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class RoomResponseMapper {

    private RoomResponseMapper() {
    }

    public static RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId().value())
                .game(toGameResponse(room.getGame()))
                .players(
                        room.getPlayers().entrySet().stream()
                                .collect(Collectors.toMap(
                                        entry -> entry.getKey().value(),
                                        entry -> toPlayerResponse(entry.getValue())
                                ))
                )
                .lastActivityTime(room.getLastActivityTime())
                .timePlayerLeft(room.getTimePlayerLeft())
                .build();
    }

    private static GameResponse toGameResponse(Game game) {
        if (game == null) {
            return null;
        }

        return GameResponse.builder()
                .player1(toPlayerResponse(game.getPlayer1()))
                .player2(toPlayerResponse(game.getPlayer2()))
                .absentPlayerId(game.getAbsentPlayerId() != null ? game.getAbsentPlayerId().value() : null)
                .board(toBoardResponse(game.getBoard()))
                .gameStatus(game.getGameStatus() != null ? game.getGameStatus().name() : null)
                .previousStatus(game.getPreviousStatus() != null ? game.getPreviousStatus().name() : null)
                .winner(game.getWinner())
                .build();
    }

    private static BoardResponse toBoardResponse(Board board) {
        if (board == null) {
            return null;
        }

        return BoardResponse.builder()
                .pits(Arrays.stream(board.getPits()).map(RoomResponseMapper::toPitResponse).toList())
                .player1Score(board.getPlayer1Score())
                .player2Score(board.getPlayer2Score())
                .build();
    }

    private static PitResponse toPitResponse(Pit pit) {
        if (pit == null) {
            return null;
        }

        return PitResponse.builder()
                .stones(pit.getStones())
                .build();
    }

    private static PlayerResponse toPlayerResponse(Player player) {
        if (player == null) {
            return null;
        }

        return PlayerResponse.builder()
                .name(player.getName())
                .id(player.getId().value())
                .stones(player.getStones())
                .build();
    }
}