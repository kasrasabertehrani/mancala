
package com.mancalagame.infrastructure.adapter.in.websocket;

import com.mancalagame.application.service.GameService;
import com.mancalagame.infrastructure.SessionTracker;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final SessionTracker sessionTracker;
    private final GameService gameService;

    public WebSocketEventListener(SessionTracker sessionTracker, GameService gameService) {
        this.sessionTracker = sessionTracker;
        this.gameService = gameService;
    }

    // UPDATED WebSocketEventListener.java
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String brokenSessionId = event.getSessionId(); // Grab the specific ID that broke

        String playerId = sessionTracker.getPlayerId(brokenSessionId);
        String roomId = sessionTracker.getRoomId(brokenSessionId);

        if (roomId != null && playerId != null) {

            String activeSession = sessionTracker.getActiveSessionForPlayer(playerId);
            if (activeSession != null && !activeSession.equals(brokenSessionId)) {
                // It's a ghost disconnect! Ignore it and don't bother the Service.
                return;
            }

            // Pass the broken session ID down the chain!
            gameService.handlePlayerDisconnect(roomId, playerId);

            sessionTracker.removeSession(brokenSessionId);
        }
    }
}
