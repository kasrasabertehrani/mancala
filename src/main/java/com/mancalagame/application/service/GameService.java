package com.mancalagame.application.service;

import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.application.port.out.GameRoomRepositoryPort;
import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.model.Game;
import com.mancalagame.domain.model.GameRoom;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    // The Service relies purely on interfaces (Ports), not implementations!
    private final GameRoomRepositoryPort roomRepository;
    private final DomainEventPublisherPort eventPublisher;

    public GameService(GameRoomRepositoryPort roomRepository, DomainEventPublisherPort eventPublisher) {
        this.roomRepository = roomRepository;
        this.eventPublisher = eventPublisher;
    }

    public GameRoom makeMove(String roomId, String playerId, int pitIndex) {
        GameRoom room = getRoomOrThrow(roomId);

        synchronized (room) {
            // 1. The Domain handles the pure business logic
            room.makeMove(playerId, pitIndex);

            // 2. The Port saves the state (implemented by your Infrastructure Adapter)
            roomRepository.save(room);

            // 3. The Port broadcasts the events
            publishEvents(room);

            // 4. Cleanup if the game naturally ended
            if (room.getGame().getGameStatus() == Game.GameStatus.GAME_OVER) {
                roomRepository.deleteById(roomId);
            }
            return room;
        }
    }

    // UPDATED GameService.java
    public GameRoom handlePlayerDisconnect(String roomId, String playerId) {
        GameRoom room = roomRepository.findById(roomId);
        if (room == null) return null;

        synchronized (room) {
            // Pass the broken session down to the shield
            room.playerLeftTable(playerId);

            roomRepository.save(room);
            publishEvents(room);
            return room;
        }
    }

    public GameRoom handlePlayerReconnect(String roomId, String playerId) {
        GameRoom room = getRoomOrThrow(roomId); // Use your helper method!

        synchronized (room) {
            room.playerReturned(playerId);
            roomRepository.save(room);
            publishEvents(room);
            return room;
        }
    }

    /**
     * Called by the Scheduler adapter. Asks every room to check its own timers.
     */
    public List<GameRoom> processTimeouts() {
        Instant now = Instant.now();
        List<GameRoom> timedOutRooms = new ArrayList<>();

        for (GameRoom room : roomRepository.findAll()) {
            synchronized (room) {
                if (room.hasReconnectTimedOut(now)) {
                    room.forceForfeit(room.getGame().getAbsentPlayerId(), "Disconnect grace period expired.");
                    roomRepository.save(room);
                    publishEvents(room);
                    timedOutRooms.add(room);
                    roomRepository.deleteById(room.getRoomId());
                }
                else if (room.hasInactivityTimedOut(now)) {
                    String idlePlayerId = (room.getGame().getGameStatus() == Game.GameStatus.PLAYER_1_TURN)
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

    // --- HELPER METHODS ---

    private GameRoom getRoomOrThrow(String roomId) {
        GameRoom room = roomRepository.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return room;
    }

    private void publishEvents(GameRoom room) {
        // Pull all the uncommitted events out of the room's outbox
        List<DomainEvent> events = room.getUncommittedEvents();

        // Hand them off to the unknown infrastructure layer to deal with
        for (DomainEvent event : events) {
            eventPublisher.publish(event);
        }
    }
}