package com.mancalagame.domain.model;

import com.mancalagame.domain.event.DomainEvent;
import com.mancalagame.domain.event.PlayerLeftTableEvent; // Rename your event class!
import com.mancalagame.domain.event.PlayerForfeitedEvent;
import com.mancalagame.domain.event.PlayerReturnedEvent; // Rename your event class!
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
    private final List<DomainEvent> domainEvents;

    private Instant lastActivityTime;
    private Instant timePlayerLeft; // <-- Domain Language!

    private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration RECONNECT_GRACE_PERIOD = Duration.ofSeconds(30);

    public GameRoom(String roomId, Player playerOne) {
        this.roomId = roomId;
        this.game = new Game(playerOne);
        this.players = new ConcurrentHashMap<>();
        this.domainEvents = Collections.synchronizedList(new ArrayList<>());
        this.lastActivityTime = Instant.now();
        this.players.put(playerOne.getId(), playerOne);
    }

    public void addPlayer(Player playerTwo) {
        if (players.size() >= 2) throw new IllegalStateException("Room is full");
        players.put(playerTwo.getId(), playerTwo);
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

    // --- TRANSLATED TO UBIQUITOUS LANGUAGE ---

    public void playerLeftTable(String playerId) {
        validatePlayer(playerId);
        this.timePlayerLeft = Instant.now();
        game.markPlayerAbsent(playerId);

        // Note: You will need to rename PlayerDisconnectedEvent -> PlayerLeftTableEvent
        domainEvents.add(new PlayerLeftTableEvent(roomId, playerId));
    }

    public void playerReturned(String playerId) {
        validatePlayer(playerId);

        boolean wasSuspended = (game.getGameStatus() == Game.GameStatus.MATCH_SUSPENDED)
                && playerId.equals(game.getAbsentPlayerId());

        recordActivity();

        if (wasSuspended) {
            this.timePlayerLeft = null;
            game.markPlayerReturned(playerId);

            // Note: You will need to rename PlayerReconnectedEvent -> PlayerReturnedEvent
            domainEvents.add(new PlayerReturnedEvent(roomId, playerId));
        }
    }

    // --- TIMEOUT EVALUATION ---

    public boolean hasInactivityTimedOut(Instant now) {
        return Duration.between(lastActivityTime, now).compareTo(INACTIVITY_TIMEOUT) > 0;
    }

    public boolean hasReconnectTimedOut(Instant now) {
        if (timePlayerLeft == null) return false;
        return Duration.between(timePlayerLeft, now).compareTo(RECONNECT_GRACE_PERIOD) > 0;
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
    public Map<String, Player> getPlayers() { return new HashMap<>(players); }
}