package com.mancalagame.infrastructure;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionTracker {
    // Stores: "abc-123" -> "player-uuid-1"
    private final ConcurrentHashMap<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    // Stores: "abc-123" -> "room-5"
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();

    // NEW: Tracks the player's currently active network pipe
    private final ConcurrentHashMap<String, String> playerToActiveSession = new ConcurrentHashMap<>();

    public void trackSession(String sessionId, String playerId, String roomId) {
        sessionToPlayer.put(sessionId, playerId);
        sessionToRoom.put(sessionId, roomId);
        playerToActiveSession.put(playerId, sessionId);
    }

    public String getActiveSessionForPlayer(String playerId) {
        return playerToActiveSession.get(playerId);
    }
    public String getPlayerId(String sessionId) { return sessionToPlayer.get(sessionId); }
    public String getRoomId(String sessionId) { return sessionToRoom.get(sessionId); }

    public void removeSession(String sessionId) {
        String playerId = sessionToPlayer.remove(sessionId);
        sessionToRoom.remove(sessionId);

        // Clean up player's active session if this was their current one
        if (playerId != null && sessionId.equals(playerToActiveSession.get(playerId))) {
            playerToActiveSession.remove(playerId);
        }
    }

    // THE FIX: Prevents the ConcurrentModificationException Memory Leak!
    public void removeSessionsByRoomId(String roomId) {
        List<String> sessionsToRemove = new ArrayList<>();

        sessionToRoom.forEach((sessionId, room) -> {
            if (room.equals(roomId)) {
                sessionsToRemove.add(sessionId);
            }
        });

        for (String sessionId : sessionsToRemove) {
            removeSession(sessionId);
        }
    }
}