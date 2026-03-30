package com.mancalagame.domain.model;

import com.mancalagame.domain.event.*;
import com.mancalagame.domain.exception.InvalidGameStateException;
import com.mancalagame.domain.exception.InvalidPlayerException;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameRoom {
    private final RoomId roomId; // <-- Using Value Object
    private final Game game;
    private final Map<PlayerId, Player> players; // <-- Using Value Object as Key
    private final List<DomainEvent> domainEvents;

    private Instant lastActivityTime;
    private Instant timePlayerLeft;

    private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration RECONNECT_GRACE_PERIOD = Duration.ofSeconds(30);

    public GameRoom(RoomId roomId, Player playerOne) {
        this.roomId = roomId;
        this.game = new Game(playerOne);
        this.players = new ConcurrentHashMap<>();
        this.domainEvents = Collections.synchronizedList(new ArrayList<>());
        this.lastActivityTime = Instant.now();
        this.players.put(playerOne.getId(), playerOne);
    }

    public void addPlayer(Player playerTwo) {
        if (players.size() >= 2) throw new InvalidGameStateException("Room is full");
        players.put(playerTwo.getId(), playerTwo);
        game.setPlayer2(playerTwo);

        recordActivity();
        domainEvents.add(new PlayerJoinedEvent(roomId, playerTwo.getId()));
    }

    public void makeMove(PlayerId playerId, int pitIndex) {
        validatePlayer(playerId);
        game.playTurn(playerId, pitIndex);
        recordActivity();
        domainEvents.add(new MoveMadeEvent(roomId, playerId, pitIndex, game.getGameStatus()));
    }

    public void playerLeftTable(PlayerId playerId) {
        validatePlayer(playerId);
        this.timePlayerLeft = Instant.now();
        game.markPlayerAbsent(playerId);

        domainEvents.add(new PlayerLeftTableEvent(roomId, playerId));
    }

    public void playerReturned(PlayerId playerId) {
        validatePlayer(playerId);

        boolean wasSuspended = (game.getGameStatus() == Game.GameStatus.MATCH_SUSPENDED)
                && playerId.equals(game.getAbsentPlayerId());

        if (!wasSuspended) {
            throw new InvalidGameStateException("Player " + playerId.value() + " was not absent and cannot return.");
        }

        recordActivity();
        this.timePlayerLeft = null;
        game.markPlayerReturned(playerId);

        domainEvents.add(new PlayerReturnedEvent(roomId, playerId));
    }

    public boolean hasInactivityTimedOut(Instant now) {
        return Duration.between(lastActivityTime, now).compareTo(INACTIVITY_TIMEOUT) > 0;
    }

    public boolean hasReconnectTimedOut(Instant now) {
        if (timePlayerLeft == null) return false;
        return Duration.between(timePlayerLeft, now).compareTo(RECONNECT_GRACE_PERIOD) > 0;
    }

    public void forceForfeit(PlayerId playerId, String reason) {
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

    private void validatePlayer(PlayerId playerId) {
        if (!players.containsKey(playerId)) {
            throw new InvalidPlayerException(playerId.value(), "Player not in this room");
        }
    }

    public RoomId getRoomId() { return roomId; }
    public Game getGame() { return game; }
    public Map<PlayerId, Player> getPlayers() { return new HashMap<>(players); }
}