package com.mancalagame.infrastructure.adapter.out.event;

import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.model.GameRoom;
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

    /**
     * This method automatically catches ANY event that extends DomainEvent!
     * MoveMadeEvent, PlayerDisconnectedEvent, PlayerForfeitedEvent... it catches them all.
     */
    @EventListener
    public void handleGameEvents(DomainEvent event) {
        // 1. Wrap the event's raw String in our safe Value Object
        RoomId roomId = event.getRoomId();

        // 2. Use .ifPresent() to elegantly handle the Optional return type
        roomRepository.findById(roomId).ifPresent(room -> {

            // 3. Broadcast the state. Notice we use room.getRoomId().value() for the URL!
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId().value(), room.getGame());

            // 2. Channel B: Broadcast the raw Event (Triggers popups on the frontend)
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId().value() + "/events", event);

        });
    }
}