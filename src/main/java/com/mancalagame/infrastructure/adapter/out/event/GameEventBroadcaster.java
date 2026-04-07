package com.mancalagame.infrastructure.adapter.out.event;

import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.application.port.out.GameRoomRepositoryPort;
import com.mancalagame.domain.model.vo.RoomId;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Component
public class GameEventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameRoomRepositoryPort roomRepository;

    public GameEventBroadcaster(SimpMessagingTemplate messagingTemplate, GameRoomRepositoryPort roomRepository) {
        this.messagingTemplate = messagingTemplate;
        this.roomRepository = roomRepository;
    }

    @EventListener
    public void handleGameEvents(DomainEvent event) {
        RoomId roomId = event.getRoomId();
        roomRepository.findById(roomId).ifPresent(room -> {
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId().value(), room.getGame());
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId().value() + "/events", event);
        });
    }
}