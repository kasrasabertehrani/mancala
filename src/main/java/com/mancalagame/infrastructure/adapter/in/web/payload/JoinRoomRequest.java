package com.mancalagame.infrastructure.adapter.in.web.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request to join an existing game room")
public class JoinRoomRequest {

    @NotBlank(message = "Room ID is required")
    @Schema(description = "Room identifier", example = "abc123")
    private String roomId;

    @NotBlank(message = "Player name is required")
    @Size(min = 1, max = 50, message = "Player name must be between 1 and 50 characters")
    @Schema(description = "Player name", example = "Bob")
    private String playerName;
}