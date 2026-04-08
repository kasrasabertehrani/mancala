package com.mancalagame.infrastructure.adapter.in.scheduler;

import com.mancalagame.application.port.in.GameUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class GameTimeoutScheduler {

    private final GameUseCase gameService;

    public GameTimeoutScheduler(GameUseCase gameService) {
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 5000)
    public void checkTimeouts() {
        gameService.processTimeouts();
    }
}