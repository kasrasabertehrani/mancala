package com.mancalagame.model;

import com.mancalagame.event.*;

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

        this.players.put(playerOne.getId(), playerOne);
        this.playerSessions.put(playerOne.getId(), playerOneSessionId);
    }

    public void addPlayer(Player playerTwo, String sessionId) {
        if (players.size() >= 2) throw new IllegalStateException("Room is full");

        players.put(playerTwo.getId(), playerTwo);
        playerSessions.put(playerTwo.getId(), sessionId);
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

    // --- CONNECTION LIFECYCLE ---

    public void handleDisconnect(String playerId) {
        validatePlayer(playerId);
        playerSessions.remove(playerId);

        this.disconnectedTime = Instant.now();
        game.handleDisconnect(playerId); // Tell the Game to pause

        domainEvents.add(new PlayerDisconnectedEvent(roomId, playerId));
    }

    public void handleReconnect(String playerId, String sessionId) {
        validatePlayer(playerId);
        playerSessions.put(playerId, sessionId);

        this.disconnectedTime = null;
        game.handleReconnect(playerId); // Tell the Game to resume
        recordActivity();

        domainEvents.add(new PlayerReconnectedEvent(roomId, playerId));
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