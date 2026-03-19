package com.mancalagame.infrastructure;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionTracker {
    // Stores: "abc-123" -> "player-uuid-1"
    private final ConcurrentHashMap<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    // Stores: "abc-123" -> "room-5"
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();

    public void trackSession(String sessionId, String playerId, String roomId) {
        sessionToPlayer.put(sessionId, playerId);
        sessionToRoom.put(sessionId, roomId);
    }

    public String getPlayerId(String sessionId) { return sessionToPlayer.get(sessionId); }
    public String getRoomId(String sessionId) { return sessionToRoom.get(sessionId); }

    public void removeSession(String sessionId) {
        sessionToPlayer.remove(sessionId);
        sessionToRoom.remove(sessionId);
    }

    public void removeSessionsByRoomId(String roomId) {
        sessionToRoom.forEach((sessionId, room) -> {
            if (room.equals(roomId)) {
                sessionToPlayer.remove(sessionId);
                sessionToRoom.remove(sessionId);
            }
        });
    }
}