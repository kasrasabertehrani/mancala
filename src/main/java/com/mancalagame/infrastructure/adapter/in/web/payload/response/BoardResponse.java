package com.mancalagame.infrastructure.adapter.in.web.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "Board response")
public class BoardResponse {

    private List<PitResponse> pits;

    @Schema(example = "0")
    private int player1Score;

    @Schema(example = "0")
    private int player2Score;
}