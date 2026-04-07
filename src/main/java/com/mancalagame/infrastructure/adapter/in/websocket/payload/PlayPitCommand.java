package com.mancalagame.infrastructure.adapter.in.websocket.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class PlayPitCommand {

    @NotBlank(message = "Room ID is required and cannot be empty")
    private String roomId;

    @NotBlank(message = "Player ID is required and cannot be empty")
    private String playerId;

    @NotNull(message = "Pit index is required")
    @Min(value = 0, message = "Pit index cannot be less than 0")
    @Max(value = 13, message = "Pit index cannot be greater than 13")
    private Integer pitIndex;
}