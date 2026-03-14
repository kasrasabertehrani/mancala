package com.mancalagame.infrastructure.adapter.out.event;

import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.model.GameRoom;
import com.mancalagame.application.port.out.GameRoomRepositoryPort;
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
        // 1. Find the room where the event happened
        GameRoom room = roomRepository.findById(event.getRoomId());

        if (room != null) {
            // 2. Broadcast the fresh, updated Game state to everyone in that room
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), room.getGame());
        }
    }
}