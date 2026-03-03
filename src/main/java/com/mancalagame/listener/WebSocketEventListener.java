
package com.mancalagame.listener;

import com.mancalagame.model.GameRoom;
import com.mancalagame.service.GameService;
import com.mancalagame.service.SessionTracker;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final SessionTracker sessionTracker;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SessionTracker sessionTracker, GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.sessionTracker = sessionTracker;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        String playerId = sessionTracker.getPlayerId(sessionId);
        String roomId = sessionTracker.getRoomId(sessionId);

        if (roomId != null && playerId != null) {
            GameRoom updatedRoom = gameService.handlePlayerDisconnect(roomId, playerId);

            if (updatedRoom != null) {
                messagingTemplate.convertAndSend("/topic/room/" + roomId, updatedRoom.getGame());
            }

            sessionTracker.removeSession(sessionId);
        }
    }
}
