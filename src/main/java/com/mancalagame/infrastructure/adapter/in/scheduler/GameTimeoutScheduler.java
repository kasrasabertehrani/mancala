package com.mancalagame.infrastructure.adapter.in.scheduler;

import com.mancalagame.application.service.GameService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GameTimeoutScheduler {

    private final GameService gameService;

    public GameTimeoutScheduler(GameService gameService) {
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 5000)
    public void checkTimeouts() {
        // The GameService handles checking the timers and publishing the DomainEvents.
        // The GameEventBroadcaster will automatically catch the timeouts and send the WebSocket messages!
        gameService.processTimeouts();
    }
}