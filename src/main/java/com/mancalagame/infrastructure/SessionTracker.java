package com.mancalagame.infrastructure;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;


@Service
public class SessionTracker {

    private final ConcurrentHashMap<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> playerToActiveSession = new ConcurrentHashMap<>();

    public void trackSession(String sessionId, String playerId, String roomId) {
        sessionToPlayer.put(sessionId, playerId);
        sessionToRoom.put(sessionId, roomId);
        playerToActiveSession.put(playerId, sessionId);
    }

    public String getActiveSessionForPlayer(String playerId) {
        return playerToActiveSession.get(playerId);
    }

    public String getPlayerId(String sessionId) {
        return sessionToPlayer.get(sessionId);
    }

    public String getRoomId(String sessionId) {
        return sessionToRoom.get(sessionId);
    }

    public void removeSession(String sessionId) {
        String playerId = sessionToPlayer.remove(sessionId);
        sessionToRoom.remove(sessionId);
        if (playerId != null && sessionId.equals(playerToActiveSession.get(playerId))) {
            playerToActiveSession.remove(playerId);
        }
    }
}