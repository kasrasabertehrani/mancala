package com.mancalagame.application.service;

import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.RoomLockPort;
import com.mancalagame.application.port.out.RoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.exception.RoomNotFoundException;
import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private RoomRepositoryPort roomRepository;

    @Mock
    private RoomLockPort roomLock;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @Mock
    private Room room;

    @Mock
    private Game game;

    @Mock
    private DomainEvent event1;

    @Mock
    private DomainEvent event2;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        when(roomLock.executeWithLock(any(), any())).thenAnswer(invocation -> {
            RoomLockPort.LockableOperation<?> operation = invocation.getArgument(1);
            return operation.execute();
        });
        gameService = new GameService(roomRepository, eventPublisher, roomLock);
    }

    @Test
    void makeMove_shouldSaveRoomPublishEventsAndReturnRoom_whenGameIsNotOver() {
        RoomId roomId = new RoomId("room-1");
        PlayerId playerId = new PlayerId("player-1");
        int pitIndex = 3;

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getUncommittedEvents()).thenReturn(List.of(event1, event2));
        when(room.getGame()).thenReturn(game);
        when(game.getGameStatus()).thenReturn(Game.GameStatus.PLAYER_1_TURN);

        Room result = gameService.makeMove(roomId, playerId, pitIndex);

        assertSame(room, result);

        verify(roomRepository).findById(roomId);
        verify(room).makeMove(playerId, pitIndex);
        verify(roomRepository).save(room);
        verify(eventPublisher).publish(event1);
        verify(eventPublisher).publish(event2);
        verify(roomRepository, never()).deleteById(any());

        InOrder inOrder = inOrder(room, roomRepository, eventPublisher, roomRepository, game);
        inOrder.verify(room).makeMove(playerId, pitIndex);
        inOrder.verify(roomRepository).save(room);
        inOrder.verify(eventPublisher).publish(event1);
        inOrder.verify(eventPublisher).publish(event2);
        inOrder.verify(room).getGame();
        inOrder.verify(game).getGameStatus();
    }

    @Test
    void makeMove_shouldDeleteRoom_whenGameIsOver() {
        RoomId roomId = new RoomId("room-1");
        PlayerId playerId = new PlayerId("player-1");
        int pitIndex = 3;

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getUncommittedEvents()).thenReturn(List.of());
        when(room.getGame()).thenReturn(game);
        when(game.getGameStatus()).thenReturn(Game.GameStatus.GAME_OVER);

        Room result = gameService.makeMove(roomId, playerId, pitIndex);

        assertSame(room, result);

        verify(room).makeMove(playerId, pitIndex);
        verify(roomRepository).save(room);
        verify(roomRepository).deleteById(roomId);
    }

    @Test
    void makeMove_shouldThrowRoomNotFoundException_whenRoomDoesNotExist() {
        RoomId roomId = new RoomId("room-1");
        PlayerId playerId = new PlayerId("player-1");
        int pitIndex = 3;

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class,
                () -> gameService.makeMove(roomId, playerId, pitIndex));

        verify(roomRepository).findById(roomId);
        verify(roomRepository, never()).save(any());
        verify(roomRepository, never()).deleteById(any());
        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(room);
    }

    @Test
    void handlePlayerDisconnect_shouldSaveRoomPublishEventsAndReturnRoom_whenRoomExists() {
        RoomId roomId = new RoomId("room-1");
        PlayerId playerId = new PlayerId("player-1");

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getUncommittedEvents()).thenReturn(List.of(event1, event2));

        Room result = gameService.handlePlayerDisconnect(roomId, playerId);

        assertSame(room, result);

        verify(roomRepository).findById(roomId);
        verify(room).playerLeftTable(playerId);
        verify(roomRepository).save(room);
        verify(eventPublisher).publish(event1);
        verify(eventPublisher).publish(event2);
    }

    @Test
    void handlePlayerDisconnect_shouldReturnNull_whenRoomDoesNotExist() {
        RoomId roomId = new RoomId("room-1");
        PlayerId playerId = new PlayerId("player-1");

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        Room result = gameService.handlePlayerDisconnect(roomId, playerId);

        assertNull(result);

        verify(roomRepository).findById(roomId);
        verify(roomRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(room);
    }

    @Test
    void handlePlayerReconnect_shouldSaveRoomPublishEventsAndReturnRoom_whenRoomExists() {
        RoomId roomId = new RoomId("room-1");
        PlayerId playerId = new PlayerId("player-1");

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getUncommittedEvents()).thenReturn(List.of(event1, event2));

        Room result = gameService.handlePlayerReconnect(roomId, playerId);

        assertSame(room, result);

        verify(roomRepository).findById(roomId);
        verify(room).playerReturned(playerId);
        verify(roomRepository).save(room);
        verify(eventPublisher).publish(event1);
        verify(eventPublisher).publish(event2);
    }

    @Test
    void handlePlayerReconnect_shouldThrowRoomNotFoundException_whenRoomDoesNotExist() {
        RoomId roomId = new RoomId("room-1");
        PlayerId playerId = new PlayerId("player-1");

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class,
                () -> gameService.handlePlayerReconnect(roomId, playerId));

        verify(roomRepository).findById(roomId);
        verify(roomRepository, never()).save(any());
        verify(roomRepository, never()).deleteById(any());
        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(room);
    }

    @Test
    void processTimeouts_shouldForfeitAndDeleteRoom_whenReconnectTimedOut() {
        PlayerId absentPlayerId = new PlayerId("player-absent");
        RoomId roomId = new RoomId("room-1");

        when(room.getRoomId()).thenReturn(roomId);

        when(roomRepository.findAll()).thenReturn(List.of(room));

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        when(room.hasReconnectTimedOut(any())).thenReturn(true);
        when(room.getGame()).thenReturn(game);
        when(game.getAbsentPlayerId()).thenReturn(absentPlayerId);
        when(room.getUncommittedEvents()).thenReturn(List.of(event1, event2));

        List<Room> result = gameService.processTimeouts();

        assertEquals(1, result.size());
        assertSame(room, result.get(0));

        verify(room).hasReconnectTimedOut(any());
        verify(room).forceForfeit(absentPlayerId, "Disconnect grace period expired.");
        verify(roomRepository).save(room);
        verify(eventPublisher).publish(event1);
        verify(eventPublisher).publish(event2);
        verify(roomRepository).deleteById(roomId);

        verify(room, never()).hasInactivityTimedOut(any());
    }

    @Test
    void processTimeouts_shouldForfeitAndDeleteRoom_whenInactivityTimedOutAndPlayer1Turn() {
        PlayerId idlePlayerId = new PlayerId("player-1");
        RoomId roomId = new RoomId("room-1");

        when(room.getRoomId()).thenReturn(roomId);
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        when(room.hasReconnectTimedOut(any())).thenReturn(false);
        when(room.hasInactivityTimedOut(any())).thenReturn(true);

        when(room.getGame()).thenReturn(game);
        when(game.getGameStatus()).thenReturn(Game.GameStatus.PLAYER_1_TURN);

        Player player1 = mock(Player.class);
        when(game.getPlayer1()).thenReturn(player1);
        when(player1.getId()).thenReturn(idlePlayerId);

        when(room.getUncommittedEvents()).thenReturn(List.of(event1));

        List<Room> result = gameService.processTimeouts();

        assertEquals(1, result.size());
        verify(room).forceForfeit(idlePlayerId, "Inactivity timeout.");
        verify(roomRepository).save(room);
        verify(roomRepository).deleteById(roomId);
    }

    @Test
    void processTimeouts_shouldForfeitAndDeleteRoom_whenInactivityTimedOutAndPlayer2Turn() {
        PlayerId idlePlayerId = new PlayerId("player-2");
        RoomId roomId = new RoomId("room-1");

        when(room.getRoomId()).thenReturn(roomId);
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        when(room.hasReconnectTimedOut(any())).thenReturn(false);
        when(room.hasInactivityTimedOut(any())).thenReturn(true);

        when(room.getGame()).thenReturn(game);
        when(game.getGameStatus()).thenReturn(Game.GameStatus.PLAYER_2_TURN); // Player 2 turn!

        Player player2 = mock(Player.class);
        when(game.getPlayer2()).thenReturn(player2);
        when(player2.getId()).thenReturn(idlePlayerId);

        when(room.getUncommittedEvents()).thenReturn(List.of(event1));

        List<Room> result = gameService.processTimeouts();

        assertEquals(1, result.size());
        verify(room).forceForfeit(idlePlayerId, "Inactivity timeout.");
        verify(roomRepository).save(room);
        verify(roomRepository).deleteById(roomId);
    }

    @Test
    void processTimeouts_shouldIgnoreRoom_whenNoTimeoutOccurred() {
        RoomId roomId = new RoomId("room-1");

        when(room.getRoomId()).thenReturn(roomId);
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        when(room.hasReconnectTimedOut(any())).thenReturn(false);
        when(room.hasInactivityTimedOut(any())).thenReturn(false);

        List<Room> result = gameService.processTimeouts();

        assertEquals(0, result.size());
        verify(roomRepository, never()).save(any());
        verify(roomRepository, never()).deleteById(any());
    }
}