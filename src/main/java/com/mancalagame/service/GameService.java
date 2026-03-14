package com.mancalagame.service;

import com.mancalagame.model.Game;
import com.mancalagame.model.GameRoom;
import com.mancalagame.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    private final RoomService roomService;
    private final ApplicationEventPublisher eventPublisher; // Spring's built-in event bus!

    public GameService(RoomService roomService, ApplicationEventPublisher eventPublisher) {
        this.roomService = roomService;
        this.eventPublisher = eventPublisher;
    }

    public GameRoom makeMove(String roomId, String playerId, int pitIndex) {
        GameRoom room = getRoomOrThrow(roomId);

        synchronized (room) {
            room.makeMove(playerId, pitIndex); // The Domain handles the logic and timers
            publishEvents(room);               // Grab the events and fire them off!

            if (room.getGame().getGameStatus() == Game.GameStatus.GAME_OVER) {
                roomService.removeRoom(roomId);
            }
            return room;
        }
    }

    public GameRoom handlePlayerDisconnect(String roomId, String playerId) {
        GameRoom room = roomService.getRoom(roomId);
        if (room == null) return null;

        synchronized (room) {
            room.handleDisconnect(playerId);
            publishEvents(room);
            return room;
        }
    }

    public GameRoom handlePlayerReconnect(String roomId, String playerId, String sessionId) {
        GameRoom room = roomService.getRoom(roomId);
        if (room == null) return null;

        synchronized (room) {
            room.handleReconnect(playerId, sessionId);
            publishEvents(room);
            return room;
        }
    }

    /**
     * Called by the Scheduler. Asks every room to check its own timers.
     */
    public List<GameRoom> processTimeouts() {
        Instant now = Instant.now();
        List<GameRoom> timedOutRooms = new ArrayList<>();

        for (GameRoom room : roomService.getAllRooms()) {
            synchronized (room) {
                if (room.hasReconnectTimedOut(now)) {
                    room.forceForfeit(room.getGame().getDisconnectedPlayerId(), "Disconnect grace period expired.");
                    publishEvents(room);
                    timedOutRooms.add(room);
                    roomService.removeRoom(room.getRoomId());
                }
                else if (room.hasInactivityTimedOut(now)) {
                    // Find out whose turn it was so we can penalize them
                    String idlePlayerId = (room.getGame().getGameStatus() == Game.GameStatus.PLAYER_1_TURN)
                            ? room.getGame().getPlayer1().getId()
                            : room.getGame().getPlayer2().getId();

                    room.forceForfeit(idlePlayerId, "Inactivity timeout.");
                    publishEvents(room);
                    timedOutRooms.add(room);
                    roomService.removeRoom(room.getRoomId());
                }
            }
        }

        return timedOutRooms;
    }

    // --- HELPER METHODS ---

    private GameRoom getRoomOrThrow(String roomId) {
        GameRoom room = roomService.getRoom(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return room;
    }

    private void publishEvents(GameRoom room) {
        // Pull all the uncommitted events out of the room's outbox
        List<DomainEvent> events = room.getUncommittedEvents();

        // Shout them out to the rest of the Spring application!
        for (DomainEvent event : events) {
            eventPublisher.publishEvent(event);
        }
    }
}