package com.mancalagame.infrastructure.adapter.in.web.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Game response")
public class GameResponse {

    private PlayerResponse player1;

    private PlayerResponse player2;

    @Schema(example = "d9f1af18-1170-42d3-a675-499148aa69f4", nullable = true)
    private String absentPlayerId;

    private BoardResponse board;

    @Schema(example = "WAITING_FOR_PLAYER_2")
    private String gameStatus;

    @Schema(nullable = true)
    private String previousStatus;

    private String winner;
}