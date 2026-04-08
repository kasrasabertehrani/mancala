package com.mancalagame.infrastructure.adapter.in.web.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@Schema(description = "Room response")
public class RoomResponse {

    @Schema(example = "1")
    private String roomId;

    private GameResponse game;

    private Map<String, PlayerResponse> players;

    private Instant lastActivityTime;

    private Instant timePlayerLeft;
}
