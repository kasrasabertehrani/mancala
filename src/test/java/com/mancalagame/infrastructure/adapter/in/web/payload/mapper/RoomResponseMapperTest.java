package com.mancalagame.infrastructure.adapter.in.web.payload.mapper;

import com.mancalagame.domain.model.Board;
import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.Pit;
import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.RoomId;
import com.mancalagame.infrastructure.adapter.in.web.payload.response.RoomResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomResponseMapperTest {

    @Test
    void toResponse_shouldMapRoomCorrectly() {
        Player host = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), host);

        RoomResponse response = RoomResponseMapper.toResponse(room);

        assertNotNull(response);
        assertEquals("room-1", response.getRoomId());

        assertNotNull(response.getGame());
        assertEquals("Alice", response.getGame().getPlayer1().getName());
        assertEquals(host.getId().value(), response.getGame().getPlayer1().getId());
        assertNull(response.getGame().getPlayer2());
        assertEquals("WAITING_FOR_PLAYER_2", response.getGame().getGameStatus());

        assertNotNull(response.getGame().getBoard());
        assertEquals(14, response.getGame().getBoard().getPits().size());
        assertEquals(4, response.getGame().getBoard().getPits().get(0).getStones());
        assertEquals(0, response.getGame().getBoard().getPits().get(6).getStones());

        assertNotNull(response.getPlayers());
        assertEquals(1, response.getPlayers().size());
        assertTrue(response.getPlayers().containsKey(host.getId().value()));
        assertEquals("Alice", response.getPlayers().get(host.getId().value()).getName());

        assertNotNull(response.getLastActivityTime());
        assertNull(response.getTimePlayerLeft());
    }

    @Test
    void toResponse_shouldReturnNullGame_whenRoomGameIsNull() {
        Room room = mock(Room.class);
        RoomId roomId = new RoomId("room-without-game");
        Instant lastActivity = Instant.parse("2026-04-08T10:15:30Z");

        Player player = new Player("Bob");
        Map<com.mancalagame.domain.model.vo.PlayerId, Player> players = new HashMap<>();
        players.put(player.getId(), player);

        when(room.getRoomId()).thenReturn(roomId);
        when(room.getGame()).thenReturn(null);
        when(room.getPlayers()).thenReturn(players);
        when(room.getLastActivityTime()).thenReturn(lastActivity);
        when(room.getTimePlayerLeft()).thenReturn(null);

        RoomResponse response = RoomResponseMapper.toResponse(room);

        assertNotNull(response);
        assertEquals("room-without-game", response.getRoomId());
        assertNull(response.getGame());
        assertEquals(lastActivity, response.getLastActivityTime());
        assertEquals("Bob", response.getPlayers().get(player.getId().value()).getName());
    }

    @Test
    void toResponse_shouldMapNullableGameFields_whenGameContainsNulls() {
        Room room = mock(Room.class);
        Game game = mock(Game.class);
        RoomId roomId = new RoomId("room-nullable-game");
        Instant leftAt = Instant.parse("2026-04-08T11:00:00Z");

        when(room.getRoomId()).thenReturn(roomId);
        when(room.getGame()).thenReturn(game);
        when(room.getPlayers()).thenReturn(Map.of());
        when(room.getLastActivityTime()).thenReturn(null);
        when(room.getTimePlayerLeft()).thenReturn(leftAt);

        Player absent = new Player("Carol");
        when(game.getPlayer1()).thenReturn(null);
        when(game.getPlayer2()).thenReturn(null);
        when(game.getAbsentPlayerId()).thenReturn(absent.getId());
        when(game.getBoard()).thenReturn(null);
        when(game.getGameStatus()).thenReturn(null);
        when(game.getPreviousStatus()).thenReturn(Game.GameStatus.PLAYER_1_TURN);
        when(game.getWinner()).thenReturn("winner-id");

        RoomResponse response = RoomResponseMapper.toResponse(room);

        assertNotNull(response.getGame());
        assertNull(response.getGame().getPlayer1());
        assertNull(response.getGame().getPlayer2());
        assertEquals(absent.getId().value(), response.getGame().getAbsentPlayerId());
        assertNull(response.getGame().getBoard());
        assertNull(response.getGame().getGameStatus());
        assertEquals("PLAYER_1_TURN", response.getGame().getPreviousStatus());
        assertEquals("winner-id", response.getGame().getWinner());
        assertEquals(leftAt, response.getTimePlayerLeft());
    }

    @Test
    void toResponse_shouldKeepNullPitEntry_whenBoardContainsNullPit() {
        Room room = mock(Room.class);
        Game game = mock(Game.class);
        Board board = mock(Board.class);

        Player player = new Player("Dave");

        when(room.getRoomId()).thenReturn(new RoomId("room-null-items"));
        when(room.getGame()).thenReturn(game);
        when(room.getPlayers()).thenReturn(Map.of(player.getId(), player));
        when(room.getLastActivityTime()).thenReturn(Instant.now());
        when(room.getTimePlayerLeft()).thenReturn(null);

        when(game.getPlayer1()).thenReturn(player);
        when(game.getPlayer2()).thenReturn(null);
        when(game.getAbsentPlayerId()).thenReturn(null);
        when(game.getBoard()).thenReturn(board);
        when(game.getGameStatus()).thenReturn(Game.GameStatus.WAITING_FOR_PLAYER_2);
        when(game.getPreviousStatus()).thenReturn(null);
        when(game.getWinner()).thenReturn(null);

        when(board.getPits()).thenReturn(new Pit[]{new Pit(3), null, new Pit(1)});
        when(board.getPlayer1Score()).thenReturn(7);
        when(board.getPlayer2Score()).thenReturn(2);

        RoomResponse response = RoomResponseMapper.toResponse(room);

        assertNotNull(response.getGame().getBoard());
        assertEquals(3, response.getGame().getBoard().getPits().size());
        assertEquals(3, response.getGame().getBoard().getPits().get(0).getStones());
        assertNull(response.getGame().getBoard().getPits().get(1));
        assertEquals(1, response.getGame().getBoard().getPits().get(2).getStones());
        assertEquals(7, response.getGame().getBoard().getPlayer1Score());
        assertEquals(2, response.getGame().getBoard().getPlayer2Score());
    }

    @Test
    void toResponse_shouldThrowNpe_whenPlayersMapContainsNullValue() {
        Room room = mock(Room.class);
        Player playerWithNullMapValue = new Player("Eve");
        Map<com.mancalagame.domain.model.vo.PlayerId, Player> players = new HashMap<>();
        players.put(playerWithNullMapValue.getId(), null);

        when(room.getRoomId()).thenReturn(new RoomId("room-null-player-map"));
        when(room.getGame()).thenReturn(null);
        when(room.getPlayers()).thenReturn(players);
        when(room.getLastActivityTime()).thenReturn(Instant.now());
        when(room.getTimePlayerLeft()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> RoomResponseMapper.toResponse(room));
    }
}