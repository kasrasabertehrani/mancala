package com.mancalagame.application.service;

import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.GameRoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.model.GameRoom;
import com.mancalagame.domain.model.Player;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoomService {

    // 1. Pure interfaces only! No ConcurrentHashMap, no SessionTracker.
    private final GameRoomRepositoryPort roomRepository;
    private final DomainEventPublisherPort eventPublisher;

    private final AtomicInteger roomCounter = new AtomicInteger(1);

    public RoomService(GameRoomRepositoryPort roomRepository, DomainEventPublisherPort eventPublisher) {
        this.roomRepository = roomRepository;
        this.eventPublisher = eventPublisher;
    }

    public GameRoom createRoom(Player host) {
        String simpleRoomId = String.valueOf(roomCounter.getAndIncrement());

        GameRoom newRoom = new GameRoom(simpleRoomId, host, null);

        // Save it using the Port, not a local Map
        roomRepository.save(newRoom);
        return newRoom;
    }

    public GameRoom joinRoom(String roomId, Player player2) {
        GameRoom room = roomRepository.findById(roomId);

        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }

        synchronized (room) {
            room.addPlayer(player2, null);

            // Save the updated state to the Port
            roomRepository.save(room);

            // Publish the PlayerJoinedEvent using the Port!
            for (DomainEvent event : room.getUncommittedEvents()) {
                eventPublisher.publish(event);
            }
        }

        return room;
    }

    public GameRoom getRoom(String roomId) {
        return roomRepository.findById(roomId);
    }

    public Collection<GameRoom> getAllRooms() {
        return roomRepository.findAll();
    }

    public void removeRoom(String roomId) {
        // Notice we don't call SessionTracker here anymore.
        // The Infrastructure Adapter handles deleting the session tracking when deleteById is called!
        roomRepository.deleteById(roomId);
    }
}