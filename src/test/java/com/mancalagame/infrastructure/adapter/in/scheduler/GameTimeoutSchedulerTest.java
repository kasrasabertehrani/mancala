package com.mancalagame.infrastructure.adapter.in.scheduler;

import com.mancalagame.application.port.in.GameUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameTimeoutSchedulerTest {

    @Mock
    private GameUseCase gameUseCase;

    @Test
    void shouldDelegateTimeoutCheckToGameUseCase() {
        GameTimeoutScheduler scheduler = new GameTimeoutScheduler(gameUseCase);
        when(gameUseCase.processTimeouts()).thenReturn(Collections.emptyList());

        assertDoesNotThrow(scheduler::checkTimeouts);

        verify(gameUseCase).processTimeouts();
    }
}
