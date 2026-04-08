
package com.mancalagame.infrastructure.adapter.in.websocket;

import com.mancalagame.application.port.in.GameUseCase;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;
import com.mancalagame.infrastructure.SessionTracker;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Component
public class WebSocketEventListener {

    private final SessionTracker sessionTracker;
    private final GameUseCase gameService;

    public WebSocketEventListener(SessionTracker sessionTracker, GameUseCase gameService) {
        this.sessionTracker = sessionTracker;
        this.gameService = gameService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String brokenSessionId = event.getSessionId();
        String playerIdStr = sessionTracker.getPlayerId(brokenSessionId);
        String roomIdStr = sessionTracker.getRoomId(brokenSessionId);

        if (roomIdStr != null && playerIdStr != null) {
            String activeSession = sessionTracker.getActiveSessionForPlayer(playerIdStr);
            if (activeSession != null && !activeSession.equals(brokenSessionId)) {
                return;
            }

            RoomId roomId = new RoomId(roomIdStr);
            PlayerId playerId = new PlayerId(playerIdStr);

            gameService.handlePlayerDisconnect(roomId, playerId);
            sessionTracker.removeSession(brokenSessionId);
        }
    }
}