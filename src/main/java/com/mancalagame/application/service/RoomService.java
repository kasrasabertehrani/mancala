package com.mancalagame.application.service;

import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.GameRoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.exception.RoomNotFoundException;
import com.mancalagame.domain.model.GameRoom;
import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.vo.RoomId;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoomService {

    private final GameRoomRepositoryPort roomRepository;
    private final DomainEventPublisherPort eventPublisher;

    private final AtomicInteger roomCounter = new AtomicInteger(1);

    public RoomService(GameRoomRepositoryPort roomRepository, DomainEventPublisherPort eventPublisher) {
        this.roomRepository = roomRepository;
        this.eventPublisher = eventPublisher;
    }

    public GameRoom createRoom(Player host) {
        String simpleRoomIdStr = String.valueOf(roomCounter.getAndIncrement());
        RoomId roomId = new RoomId(simpleRoomIdStr);

        GameRoom newRoom = new GameRoom(roomId, host);

        roomRepository.save(newRoom);
        return newRoom;
    }

    public GameRoom joinRoom(String roomIdStr, Player playerTwo) {
        RoomId roomId = new RoomId(roomIdStr);

        GameRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId.value()));

        synchronized (room) {
            room.addPlayer(playerTwo);
            roomRepository.save(room);

            for (DomainEvent event : room.getUncommittedEvents()) {
                eventPublisher.publish(event);
            }
        }

        return room;
    }

    public GameRoom getRoom(String roomIdStr) {
        RoomId roomId = new RoomId(roomIdStr);
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId.value()));
    }

    public Collection<GameRoom> getAllRooms() {
        return roomRepository.findAll();
    }

    public void removeRoom(String roomIdStr) {
        RoomId roomId = new RoomId(roomIdStr);
        roomRepository.deleteById(roomId);
    }
}