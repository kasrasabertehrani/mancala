package com.mancalagame.service;

import com.mancalagame.model.GameRoom;
import com.mancalagame.model.Player;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoomService {

    // Our in-memory "database" mapping Room IDs to GameRooms
    private final ConcurrentHashMap<String, GameRoom> activeRooms = new ConcurrentHashMap<>();
    private final AtomicInteger roomCounter = new AtomicInteger(1);
    private final SessionTracker sessionTracker;

    public RoomService(SessionTracker sessionTracker) {
        this.sessionTracker = sessionTracker;
    }

    // 1. Create a new room
    public GameRoom createRoom(Player host) {
        String simpleRoomId = String.valueOf(roomCounter.getAndIncrement());

        // We pass 'null' for the Session ID because this is an HTTP REST call.
        // The player doesn't have a WebSocket session ID until they actually connect!
        GameRoom newRoom = new GameRoom(simpleRoomId, host, null);

        activeRooms.put(simpleRoomId, newRoom);
        return newRoom;
    }

    // 2. Let a second player join
    public GameRoom joinRoom(String roomId, Player player2) {
        GameRoom room = activeRooms.get(roomId);

        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }

        // DDD MAGIC: We tell the Room to add the player. We don't touch the Game directly!
        // We synchronize it just in case Player 1 is making a move at this exact millisecond.
        synchronized (room) {
            room.addPlayer(player2, null); // Pass null for session ID until WebSocket connects
        }

        return room;
    }

    // 3. Retrieve a single room
    public GameRoom getRoom(String roomId) {
        return activeRooms.get(roomId);
    }

    // --- 4. NEW: Retrieve all rooms (Used by the GameService Scheduler) ---
    public Collection<GameRoom> getAllRooms() {
        return activeRooms.values();
    }

    // 5. Cleanup
    public void removeRoom(String roomId) {
        activeRooms.remove(roomId);
        sessionTracker.removeSessionsByRoomId(roomId);
    }
}