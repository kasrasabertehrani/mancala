package com.mancalagame.application.service;

import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.GameRoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.exception.RoomNotFoundException;
import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Service
public class GameService {

    private final GameRoomRepositoryPort roomRepository;
    private final DomainEventPublisherPort eventPublisher;

    public GameService(GameRoomRepositoryPort roomRepository, DomainEventPublisherPort eventPublisher) {
        this.roomRepository = roomRepository;
        this.eventPublisher = eventPublisher;
    }

    public Room makeMove(RoomId roomId, PlayerId playerId, int pitIndex) {
        Room room = getRoomOrThrow(roomId);
        synchronized (room) {
            room.makeMove(playerId, pitIndex);
            roomRepository.save(room);
            publishEvents(room);

            if (room.getGame().getGameStatus() == Game.GameStatus.GAME_OVER) {
                roomRepository.deleteById(roomId);
            }
            return room;
        }
    }

    public Room handlePlayerDisconnect(RoomId roomId, PlayerId playerId) {
        return roomRepository.findById(roomId).map(room -> {
            synchronized (room) {
                room.playerLeftTable(playerId);
                roomRepository.save(room);
                publishEvents(room);
                return room;
            }
        }).orElse(null);
    }

    public Room handlePlayerReconnect(RoomId roomId, PlayerId playerId) {
        Room room = getRoomOrThrow(roomId);
        synchronized (room) {
            room.playerReturned(playerId);
            roomRepository.save(room);
            publishEvents(room);
            return room;
        }
    }

    public List<Room> processTimeouts() {
        Instant now = Instant.now();
        List<Room> timedOutRooms = new ArrayList<>();

        for (Room room : roomRepository.findAll()) {
            synchronized (room) {
                if (room.hasReconnectTimedOut(now)) {
                    room.forceForfeit(room.getGame().getAbsentPlayerId(), "Disconnect grace period expired.");
                    roomRepository.save(room);
                    publishEvents(room);
                    timedOutRooms.add(room);
                    roomRepository.deleteById(room.getRoomId());
                }
                else if (room.hasInactivityTimedOut(now)) {
                    PlayerId idlePlayerId = (room.getGame().getGameStatus() == Game.GameStatus.PLAYER_1_TURN)
                            ? room.getGame().getPlayer1().getId()
                            : room.getGame().getPlayer2().getId();

                    room.forceForfeit(idlePlayerId, "Inactivity timeout.");
                    roomRepository.save(room);
                    publishEvents(room);
                    timedOutRooms.add(room);
                    roomRepository.deleteById(room.getRoomId());
                }
            }
        }

        return timedOutRooms;
    }

    private Room getRoomOrThrow(RoomId roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId.value()));
    }

    private void publishEvents(Room room) {
        List<DomainEvent> events = room.getUncommittedEvents();
        for (DomainEvent event : events) {
            eventPublisher.publish(event);
        }
    }
}