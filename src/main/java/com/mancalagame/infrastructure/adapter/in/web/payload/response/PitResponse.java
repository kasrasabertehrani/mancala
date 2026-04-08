package com.mancalagame.infrastructure.adapter.in.web.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Pit response")
public class PitResponse {

    @Schema(example = "4")
    private int stones;
}