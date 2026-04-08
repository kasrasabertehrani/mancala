package com.mancalagame.infrastructure.adapter.in.web.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Schema(description = "Request to create a game room")
public class CreateRoomRequest {

    @NotBlank(message = "Player name is required")
    @Size(min = 1, max = 50, message = "Player name must be between 1 and 50 characters")
    @Schema(description = "Host player name", example = "Alice")
    private String playerName;
}