package com.mancalagame.application.service;

import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.RoomLockPort;
import com.mancalagame.application.port.out.RoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.exception.RoomNotFoundException;
import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.RoomId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepositoryPort roomRepository;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @Mock
    private RoomLockPort roomLock;

    @Mock
    private Room room;

    @Mock
    private Player playerTwo;

    @Mock
    private DomainEvent event1;

    @Mock
    private DomainEvent event2;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        lenient().when(roomLock.executeWithLock(any(), any())).thenAnswer(invocation -> {
            RoomLockPort.LockableOperation<?> operation = invocation.getArgument(1);
            return operation.execute();
        });
        roomService = new RoomService(roomRepository, eventPublisher, roomLock);
    }

    @Test
    void createRoom_shouldCreateAndSaveRoom() {
        Player host = new Player("Alice");

        Room result = roomService.createRoom(host);

        assertNotNull(result);
        assertEquals("1", result.getRoomId().value());
        assertSame(host, result.getGame().getPlayer1());

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());

        Room savedRoom = roomCaptor.getValue();
        assertNotNull(savedRoom);
        assertEquals("1", savedRoom.getRoomId().value());
        assertSame(host, savedRoom.getGame().getPlayer1());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void joinRoom_shouldAddPlayerSaveRoomPublishEventsAndReturnRoom_whenRoomExists() {
        String roomIdStr = "1";
        RoomId roomId = new RoomId(roomIdStr);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getUncommittedEvents()).thenReturn(List.of(event1, event2));

        Room result = roomService.joinRoom(roomIdStr, playerTwo);

        assertSame(room, result);

        verify(roomRepository).findById(roomId);
        verify(room).addPlayer(playerTwo);
        verify(roomRepository).save(room);
        verify(eventPublisher).publish(event1);
        verify(eventPublisher).publish(event2);
    }

    @Test
    void joinRoom_shouldThrowRoomNotFoundException_whenRoomDoesNotExist() {
        String roomIdStr = "1";
        RoomId roomId = new RoomId(roomIdStr);

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class,
                () -> roomService.joinRoom(roomIdStr, playerTwo));

        verify(roomRepository).findById(roomId);
        verify(roomRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(room);
    }

    @Test
    void getRoom_shouldReturnRoom_whenRoomExists() {
        String roomIdStr = "1";
        RoomId roomId = new RoomId(roomIdStr);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        Room result = roomService.getRoom(roomIdStr);

        assertSame(room, result);

        verify(roomRepository).findById(roomId);
    }

    @Test
    void getRoom_shouldThrowRoomNotFoundException_whenRoomDoesNotExist() {
        String roomIdStr = "1";
        RoomId roomId = new RoomId(roomIdStr);

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class,
                () -> roomService.getRoom(roomIdStr));

        verify(roomRepository).findById(roomId);
    }
}
