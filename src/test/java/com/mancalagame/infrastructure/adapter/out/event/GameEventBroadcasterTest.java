package com.mancalagame.infrastructure.adapter.out.event;

import com.mancalagame.application.port.out.RoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.RoomId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameEventBroadcasterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RoomRepositoryPort roomRepository;

    @Mock
    private DomainEvent event;

    @Mock
    private Room room;

    @Mock
    private Game game;

    @InjectMocks
    private GameEventBroadcaster broadcaster;

    @Test
    void shouldSendGameAndEventMessagesWhenRoomExists() {
        RoomId roomId = new RoomId("42");

        when(event.getRoomId()).thenReturn(roomId);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getRoomId()).thenReturn(roomId);
        when(room.getGame()).thenReturn(game);

        broadcaster.handleGameEvents(event);

        verify(roomRepository).findById(roomId);

        InOrder inOrder = inOrder(messagingTemplate);
        inOrder.verify(messagingTemplate).convertAndSend("/topic/room/42", game);
        inOrder.verify(messagingTemplate).convertAndSend("/topic/room/42/events", event);
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void shouldNotSendMessagesWhenRoomNotFound() {
        RoomId roomId = new RoomId("42");

        when(event.getRoomId()).thenReturn(roomId);
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        broadcaster.handleGameEvents(event);

        verify(roomRepository).findById(roomId);
        verifyNoInteractions(messagingTemplate);
        verifyNoInteractions(room);
    }
}
