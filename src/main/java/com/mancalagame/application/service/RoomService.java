package com.mancalagame.application.service;

import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.GameRoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.exception.RoomNotFoundException;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.vo.RoomId;
import org.springframework.stereotype.Service;

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

    public Room createRoom(Player host) {
        String simpleRoomIdStr = String.valueOf(roomCounter.getAndIncrement());
        RoomId roomId = new RoomId(simpleRoomIdStr);
        Room newRoom = new Room(roomId, host);
        roomRepository.save(newRoom);
        return newRoom;
    }

    public Room joinRoom(String roomIdStr, Player playerTwo) {
        RoomId roomId = new RoomId(roomIdStr);

        Room room = roomRepository.findById(roomId)
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

    public Room getRoom(String roomIdStr) {
        RoomId roomId = new RoomId(roomIdStr);
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId.value()));
    }
}