package com.mancalagame.application.service;

import com.mancalagame.application.port.in.GameUseCase;
import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.RoomLockPort;
import com.mancalagame.application.port.out.RoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.exception.RoomNotFoundException;
import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GameService implements GameUseCase {

    private final RoomRepositoryPort roomRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final RoomLockPort roomLock;

    public GameService(RoomRepositoryPort roomRepository,
                       DomainEventPublisherPort eventPublisher,
                       RoomLockPort roomLock) {
        this.roomRepository = roomRepository;
        this.eventPublisher = eventPublisher;
        this.roomLock = roomLock;
    }

    @Override
    public Room makeMove(RoomId roomId, PlayerId playerId, int pitIndex) {
        return roomLock.executeWithLock(roomId, () -> {
            Room room = getRoomOrThrow(roomId);
            room.makeMove(playerId, pitIndex);
            roomRepository.save(room);
            publishEvents(room);
            log.info("event=move_made roomId={} playerId={} pitIndex={} result=ok",
                    roomId.value(), playerId.value(), pitIndex);

            if (room.getGame().getGameStatus() == Game.GameStatus.GAME_OVER) {
                roomRepository.deleteById(roomId);
                roomLock.releaseLock(roomId);
                log.info("event=game_finished roomId={} result=ok reason=game_over", roomId.value());
            }
            return room;
        });
    }

    @Override
    public Room handlePlayerDisconnect(RoomId roomId, PlayerId playerId) {
        return roomLock.executeWithLock(roomId, () -> {
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null) {
                log.warn("event=player_disconnected roomId={} playerId={} result=ignored reason=room_not_found",
                        roomId.value(), playerId.value());
                return null;
            }

            room.playerLeftTable(playerId);
            roomRepository.save(room);
            publishEvents(room);
            log.info("event=player_disconnected roomId={} playerId={} result=ok",
                    roomId.value(), playerId.value());
            return room;
        });
    }

    @Override
    public Room handlePlayerReconnect(RoomId roomId, PlayerId playerId) {
        return roomLock.executeWithLock(roomId, () -> {
            Room room = getRoomOrThrow(roomId);
            room.playerReturned(playerId);
            roomRepository.save(room);
            publishEvents(room);
            log.info("event=player_reconnected roomId={} playerId={} result=ok",
                    roomId.value(), playerId.value());
            return room;
        });
    }

    @Override
    public List<Room> processTimeouts() {
        Instant now = Instant.now();
        List<Room> timedOutRooms = new ArrayList<>();


        List<RoomId> roomIds = roomRepository.findAll().stream()
                .map(Room::getRoomId)
                .toList();


        for (RoomId roomId : roomIds) {


            roomLock.executeWithLock(roomId, () -> {


                Room room = roomRepository.findById(roomId).orElse(null);
                if (room == null) {
                    return null;
                }

                if (room.hasReconnectTimedOut(now)) {
                    PlayerId absentPlayerId = room.getGame().getAbsentPlayerId();
                    room.forceForfeit(absentPlayerId, "Disconnect grace period expired.");
                    roomRepository.save(room);
                    publishEvents(room);
                    timedOutRooms.add(room);
                    roomRepository.deleteById(room.getRoomId());
                    log.warn("event=timeout_forfeit roomId={} playerId={} reason=disconnect_grace_expired result=ok",
                            room.getRoomId().value(), absentPlayerId.value());
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
                    log.warn("event=timeout_forfeit roomId={} playerId={} reason=inactivity_timeout result=ok",
                            room.getRoomId().value(), idlePlayerId.value());
                }
                return null;
            });
        }


        for (Room room : timedOutRooms) {
            roomLock.releaseLock(room.getRoomId());
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