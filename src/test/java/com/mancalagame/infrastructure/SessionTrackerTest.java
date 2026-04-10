package com.mancalagame.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SessionTrackerTest {

    @Test
    void  shouldTrackSession() {
        SessionTracker sessionTracker = new SessionTracker();

        sessionTracker.trackSession("session1", "player1", "room1");

        assertEquals("session1", sessionTracker.getActiveSessionForPlayer("player1"));
        assertEquals("player1", sessionTracker.getPlayerId("session1"));
        assertEquals("room1", sessionTracker.getRoomId("session1"));
    }

    @Test
    void shouldRemoveSession() {
        SessionTracker sessionTracker = new SessionTracker();
        sessionTracker.trackSession("session1", "player1", "room1");

        sessionTracker.removeSession("session1");

        assertNull(sessionTracker.getActiveSessionForPlayer("player1"));
        assertNull(sessionTracker.getPlayerId("session1"));
        assertNull(sessionTracker.getRoomId("session1"));
    }

    @Test
    void shouldKeepActiveSessionWhenRemovingOldSessionOfSamePlayer() {
        SessionTracker sessionTracker = new SessionTracker();
        sessionTracker.trackSession("session1", "player1", "room1");
        sessionTracker.trackSession("session2", "player1", "room1");

        sessionTracker.removeSession("session1");

        assertEquals("session2", sessionTracker.getActiveSessionForPlayer("player1"));
        assertNull(sessionTracker.getPlayerId("session1"));
        assertNull(sessionTracker.getRoomId("session1"));
        assertEquals("player1", sessionTracker.getPlayerId("session2"));
        assertEquals("room1", sessionTracker.getRoomId("session2"));
    }

    @Test
    void shouldIgnoreRemovingUnknownSession() {
        SessionTracker sessionTracker = new SessionTracker();
        sessionTracker.trackSession("session1", "player1", "room1");

        sessionTracker.removeSession("unknown-session");

        assertEquals("session1", sessionTracker.getActiveSessionForPlayer("player1"));
        assertEquals("player1", sessionTracker.getPlayerId("session1"));
        assertEquals("room1", sessionTracker.getRoomId("session1"));
    }

}
