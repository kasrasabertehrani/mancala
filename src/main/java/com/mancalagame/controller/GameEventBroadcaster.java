package com.mancalagame.controller;

import com.mancalagame.event.DomainEvent;
import com.mancalagame.model.GameRoom;
import com.mancalagame.service.RoomService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class GameEventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    public GameEventBroadcaster(SimpMessagingTemplate messagingTemplate, RoomService roomService) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
    }

    /**
     * This method automatically catches ANY event that extends DomainEvent!
     * MoveMadeEvent, PlayerDisconnectedEvent, PlayerForfeitedEvent... it catches them all.
     */
    @EventListener
    public void handleGameEvents(DomainEvent event) {
        // 1. Find the room where the event happened
        GameRoom room = roomService.getRoom(event.getRoomId());

        if (room != null) {
            // 2. Broadcast the fresh, updated Game state to everyone in that room
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), room.getGame());
        }
    }
}