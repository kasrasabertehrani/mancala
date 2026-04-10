package com.mancalagame.application.service;

import com.mancalagame.application.port.in.RoomUseCase;
import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.RoomLockPort;
import com.mancalagame.application.port.out.RoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.exception.RoomNotFoundException;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.vo.RoomId;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoomService implements RoomUseCase {

    private final RoomRepositoryPort roomRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final RoomLockPort roomLock;

    private final AtomicInteger roomCounter = new AtomicInteger(1);

    public RoomService(RoomRepositoryPort roomRepository,
                       DomainEventPublisherPort eventPublisher,
                       RoomLockPort roomLock) {
        this.roomRepository = roomRepository;
        this.eventPublisher = eventPublisher;
        this.roomLock = roomLock;
    }

    public Room createRoom(Player host) {
        String simpleRoomIdStr = String.valueOf(roomCounter.getAndIncrement());
        RoomId roomId = new RoomId(simpleRoomIdStr);

        return roomLock.executeWithLock(roomId, () -> {
            Room newRoom = new Room(roomId, host);
            roomRepository.save(newRoom);
            return newRoom;
        });
    }

    public Room joinRoom(String roomIdStr, Player playerTwo) {
        RoomId roomId = new RoomId(roomIdStr);

        return roomLock.executeWithLock(roomId, () -> {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RoomNotFoundException(roomId.value()));

            room.addPlayer(playerTwo);
            roomRepository.save(room);
            for (DomainEvent event : room.getUncommittedEvents()) {
                eventPublisher.publish(event);
            }
            return room;
        });
    }

    public Room getRoom(String roomIdStr) {
        RoomId roomId = new RoomId(roomIdStr);

        return roomLock.executeWithLock(roomId, () ->
                roomRepository.findById(roomId)
                        .orElseThrow(() -> new RoomNotFoundException(roomId.value()))
        );
    }
}