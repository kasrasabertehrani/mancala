package com.mancalagame.infrastructure.adapter.in.web;

import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.Player;
import com.mancalagame.infrastructure.adapter.in.web.payload.CreateRoomRequest;
import com.mancalagame.infrastructure.adapter.in.web.payload.JoinRoomRequest;
import com.mancalagame.application.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "Game room management API")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "Create room")
    @ApiResponse(responseCode = "200", description = "Room created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Player host = new Player(request.getPlayerName());
        Room newRoom = roomService.createRoom(host);
        return ResponseEntity.ok(newRoom);
    }

    @Operation(summary = "Join room")
    @ApiResponse(responseCode = "200", description = "Joined successfully")
    @ApiResponse(responseCode = "404", description = "Room not found")
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@Valid @RequestBody JoinRoomRequest request) {
            Player player2 = new Player(request.getPlayerName());
            Room room = roomService.joinRoom(request.getRoomId(), player2);
            return ResponseEntity.ok(room);
    }

    @Operation(summary = "Get room by id")
    @ApiResponse(responseCode = "200", description = "Room found")
    @ApiResponse(responseCode = "404", description = "Room not found")
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(
            @Parameter(description = "Room ID", example = "abc123")
            @PathVariable String roomId) {

        Room room = roomService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }
}