package com.mancalagame.domain.model;

import com.mancalagame.domain.event.*;
import com.mancalagame.domain.exception.InvalidGameStateException;
import com.mancalagame.domain.exception.InvalidPlayerException;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoomTest {

    @Test
    void shouldAddPlayerAndPublishEvent() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Room room = new Room(new RoomId("room-1"), playerOne);

        room.addPlayer(playerTwo);

        assertTrue(room.getPlayers().containsKey(playerTwo.getId()));

        List<?> events = room.getUncommittedEvents();
        assertEquals(1, events.size());
        assertInstanceOf(PlayerJoinedEvent.class, events.get(0));

        PlayerJoinedEvent event = (PlayerJoinedEvent) events.get(0);
        assertEquals(playerTwo.getId(), event.getPlayerId());
    }

    @Test
    void shouldThrowExceptionWhenRoomIsFull() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Player playerThree = new Player("Charlie");
        Room room = new Room(new RoomId("room-1"), playerOne);

        room.addPlayer(playerTwo);

        assertThrows(InvalidGameStateException.class, () -> room.addPlayer(playerThree));
    }

    @Test
    void shouldMakeMoveAndCreateEvent() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Room room = new Room(new RoomId("room-1"),  playerOne);
        room.addPlayer(playerTwo);

        room.makeMove(playerOne.getId(), 2);

        var events = room.getUncommittedEvents();

        assertEquals(2, events.size());
        assertInstanceOf(MoveMadeEvent.class, events.get(1));

        MoveMadeEvent event = (MoveMadeEvent) events.get(1);
        assertEquals(playerOne.getId(), event.getPlayerId());
        assertEquals(2, event.getPitIndex());
    }

    @Test
    void shouldMarkPlayerAsLeftAndCreateEvent() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Room room = new Room(new RoomId("room-1"), playerOne);
        room.addPlayer(playerTwo);

        room.playerLeftTable(playerOne.getId());

        var events = room.getUncommittedEvents();

        assertEquals(2, events.size());
        assertInstanceOf(PlayerLeftTableEvent.class,  events.get(1));

        PlayerLeftTableEvent event = (PlayerLeftTableEvent) events.get(1);
        assertEquals(playerOne.getId(), event.getPlayerId());
    }

    @Test
    void shouldReturnAbsentPlayerAndCreateEvent() {
        Player playerOne = new Player("Alice");
        Player  playerTwo = new Player("Bob");
        Room room = new Room(new RoomId("room-1"), playerOne);
        room.addPlayer(playerTwo);
        room.playerLeftTable(playerOne.getId());

        room.playerReturned(playerOne.getId());

        var events = room.getUncommittedEvents();

        assertEquals(3, events.size());
        assertInstanceOf(PlayerReturnedEvent.class, events.get(2));

        PlayerReturnedEvent event = (PlayerReturnedEvent) events.get(2);
        assertEquals(playerOne.getId(), event.getPlayerId());
    }

    @Test
    void shouldThrowExceptionWhenReturningPlayerNotInRoom() {
        Player playerOne = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), playerOne);

        PlayerId unknownPlayer = new PlayerId("unknown-player");

        assertThrows(InvalidPlayerException.class, () -> room.playerReturned(unknownPlayer));
    }

    @Test
    void shouldThrowExceptionWhenPlayerWasNotAbsent() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Room room = new Room(new RoomId("room-1"), playerOne);
        room.addPlayer(playerTwo);

        assertThrows(InvalidGameStateException.class, () -> room.playerReturned(playerOne.getId()));
    }

    @Test
    void shouldThrowExceptionWhenPlayerAbsentAndReturnsWrongPlayer() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Player playerThree = new Player("Charlie");
        Room room = new Room(new RoomId("room-1"), playerOne);
        room.addPlayer(playerTwo);
        room.playerLeftTable(playerTwo.getId());

        assertThrows(InvalidPlayerException.class, () -> room.playerReturned(playerThree.getId()));
    }

    @Test
    void shouldReturnTrueWhenInactivityTimeoutExceeded() {
        Player player = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), player);

        Instant future = Instant.now().plus(Duration.ofMinutes(6));

        boolean result = room.hasInactivityTimedOut(future);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenInactivityTimeoutNotExceeded() {
        Player player = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), player);

        Instant future = Instant.now().plus(Duration.ofMinutes(2));

        boolean result = room.hasInactivityTimedOut(future);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenPlayerNeverLeft() {
        Player player = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), player);

        boolean result = room.hasReconnectTimedOut(Instant.now());

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenReconnectGracePeriodNotExceeded() {
        Player player = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), player);

        room.playerLeftTable(player.getId());

        Instant now = Instant.now().plusSeconds(10); // меньше 30 сек

        boolean result = room.hasReconnectTimedOut(now);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenReconnectGracePeriodExceeded() {
        Player player = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), player);

        room.playerLeftTable(player.getId());

        Instant now = Instant.now().plusSeconds(40);

        boolean result = room.hasReconnectTimedOut(now);

        assertTrue(result);
    }

    @Test
    void shouldForceForfeitAndCreateEvent() {
        Player playerOne = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), playerOne);

        room.forceForfeit(playerOne.getId(), "Disconnected too long");

        var events = room.getUncommittedEvents();

        assertEquals(1, events.size());
        assertInstanceOf(PlayerForfeitedEvent.class, events.get(0));

        PlayerForfeitedEvent event = (PlayerForfeitedEvent) events.get(0);
        assertEquals(playerOne.getId(), event.getPlayerId());
        assertEquals("Disconnected too long", event.getReason());
    }

    @Test
    void shouldReturnEventsAndClearThem() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Room room = new Room(new RoomId("room-1"), playerOne);

        room.addPlayer(playerTwo);

        List<DomainEvent> events = room.getUncommittedEvents();

        assertEquals(1, events.size());
        assertInstanceOf(PlayerJoinedEvent.class, events.get(0));
        assertTrue(room.getUncommittedEvents().isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoEventsExist() {
        Player playerOne = new Player("Alice");
        Room room = new Room(new RoomId("room-1"), playerOne);

        List<DomainEvent> events = room.getUncommittedEvents();

        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void shouldHandleMultipleCallsCorrectly() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Room room = new Room(new RoomId("room-1"), playerOne);

        room.addPlayer(playerTwo);

        List<DomainEvent> first = room.getUncommittedEvents();

        List<DomainEvent> second = room.getUncommittedEvents();

        assertEquals(1, first.size());
        assertTrue(second.isEmpty());
    }
}