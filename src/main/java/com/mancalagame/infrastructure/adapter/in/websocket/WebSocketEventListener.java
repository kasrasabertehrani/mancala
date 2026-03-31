
package com.mancalagame.infrastructure.adapter.in.websocket;

import com.mancalagame.application.service.GameService;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
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

        String playerIdStr = sessionTracker.getPlayerId(brokenSessionId);
        String roomIdStr = sessionTracker.getRoomId(brokenSessionId);

        if (roomIdStr != null && playerIdStr != null) {

            String activeSession = sessionTracker.getActiveSessionForPlayer(playerIdStr);
            if (activeSession != null && !activeSession.equals(brokenSessionId)) {
                // It's a ghost disconnect! Ignore it and don't bother the Service.
                return;
            }

            RoomId roomId = new RoomId(roomIdStr);
            PlayerId playerId = new PlayerId(playerIdStr);


            // Pass the broken session ID down the chain!
            gameService.handlePlayerDisconnect(roomId, playerId);

            sessionTracker.removeSession(brokenSessionId);
        }
    }
}
