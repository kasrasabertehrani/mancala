package com.mancalagame.infrastructure.adapter.in.websocket.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReconnectRequest {

    @NotBlank(message = "Room ID is required and cannot be empty")
    private String roomId;

    @NotBlank(message = "Room ID is required and cannot be empty")
    private String playerId;
}