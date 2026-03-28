package com.mancalagame.domain.model;

import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.event.PlayerDisconnectedEvent;
import com.mancalagame.domain.event.PlayerForfeitedEvent;
import com.mancalagame.domain.event.PlayerReconnectedEvent;
import com.mancalagame.domain.event.PlayerJoinedEvent;
import com.mancalagame.domain.event.MoveMadeEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameRoom {
    private final String roomId;
    private final Game game;
    private final Map<String, Player> players;
    private final Map<String, String> playerSessions;
    private final List<DomainEvent> domainEvents;

    private Instant lastActivityTime;
    private Instant disconnectedTime;

    private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration RECONNECT_GRACE_PERIOD = Duration.ofSeconds(30);

    public GameRoom(String roomId, Player playerOne, String playerOneSessionId) {
        this.roomId = roomId;
        this.game = new Game(playerOne);

        // Thread-safe collections to prevent ConcurrentModificationException
        this.players = new ConcurrentHashMap<>();
        this.playerSessions = new ConcurrentHashMap<>();
        this.domainEvents = Collections.synchronizedList(new ArrayList<>());

        this.lastActivityTime = Instant.now();

        // Add player to the room
        this.players.put(playerOne.getId(), playerOne);

        // Only add the session if we actually have one from the network layer!
        if (playerOneSessionId != null) {
            this.playerSessions.put(playerOne.getId(), playerOneSessionId);
        }
    }

    public void addPlayer(Player playerTwo, String sessionId) {
        if (players.size() >= 2) throw new IllegalStateException("Room is full");

        players.put(playerTwo.getId(), playerTwo);

        // Protect the ConcurrentHashMap from a null session ID!
        if (sessionId != null) {
            playerSessions.put(playerTwo.getId(), sessionId);
        }
        game.setPlayer2(playerTwo);

        recordActivity();
        domainEvents.add(new PlayerJoinedEvent(roomId, playerTwo.getId()));
    }

    public void makeMove(String playerId, int pitIndex) {
        validatePlayer(playerId);

        game.playTurn(playerId, pitIndex);

        recordActivity();
        domainEvents.add(new MoveMadeEvent(roomId, playerId, pitIndex, game.getGameStatus()));
    }


    // 1. Update the signature to accept the specific session that broke
    public void handleDisconnect(String playerId, String brokenSessionId) {
        validatePlayer(playerId);

        // --- RACE CONDITION SHIELD ---
        String currentSession = playerSessions.get(playerId);
        // If the player is currently sitting here with a DIFFERENT session ID,
        // ignore this delayed "ghost" disconnect!
        if (currentSession != null && !currentSession.equals(brokenSessionId)) {
            return;
        }
        // -----------------------------

        playerSessions.remove(playerId);

        this.disconnectedTime = Instant.now();
        game.handleDisconnect(playerId); // Tell the Game to pause

        domainEvents.add(new PlayerDisconnectedEvent(roomId, playerId));
    }

    // Rename from handleReconnect to bindSession
    public void bindSession(String playerId, String sessionId) {
        validatePlayer(playerId);

        // Check if they are actually recovering from a dropped connection
        boolean wasDisconnected = (game.getGameStatus() == Game.GameStatus.PAUSED_FOR_RECONNECT)
                && playerId.equals(game.getDisconnectedPlayerId());

        // Securely bind the network pipe
        playerSessions.put(playerId, sessionId);
        recordActivity();

        // ONLY trigger the reconnect logic if they were actually disconnected
        if (wasDisconnected) {
            this.disconnectedTime = null;
            game.handleReconnect(playerId);
            domainEvents.add(new PlayerReconnectedEvent(roomId, playerId));
        }
    }

    // --- TIMEOUT EVALUATION (Used by Scheduler) ---

    public boolean hasInactivityTimedOut(Instant now) {
        return Duration.between(lastActivityTime, now).compareTo(INACTIVITY_TIMEOUT) > 0;
    }

    public boolean hasReconnectTimedOut(Instant now) {
        if (disconnectedTime == null) return false;
        return Duration.between(disconnectedTime, now).compareTo(RECONNECT_GRACE_PERIOD) > 0;
    }

    public void forceForfeit(String playerId, String reason) {
        validatePlayer(playerId);
        game.forfeit(playerId);
        domainEvents.add(new PlayerForfeitedEvent(roomId, playerId, reason));
    }

    private void recordActivity() {
        this.lastActivityTime = Instant.now();
    }

    public List<DomainEvent> getUncommittedEvents() {
        synchronized (domainEvents) {
            List<DomainEvent> events = new ArrayList<>(domainEvents);
            domainEvents.clear();
            return events;
        }
    }

    private void validatePlayer(String playerId) {
        if (!players.containsKey(playerId)) {
            throw new IllegalArgumentException("Player not in this room: " + playerId);
        }
    }

    public String getRoomId() { return roomId; }
    public Game getGame() { return game; }
    public Map<String, String> getPlayerSessions() { return new HashMap<>(playerSessions); }
    public Map<String, Player> getPlayers() { return new HashMap<>(players); }
}