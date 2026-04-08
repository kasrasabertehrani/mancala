package com.mancalagame.infrastructure.adapter.in.web.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Player response")
public class PlayerResponse {

    @Schema(example = "Alice")
    private String name;

    @Schema(example = "d9f1af18-1170-42d3-a675-499148aa69f4")
    private String id;

    @Schema(example = "0")
    private int stones;
}