package com.mancalagame.infrastructure.adapter.in.web;

import com.mancalagame.domain.exception.InvalidGameStateException;
import com.mancalagame.domain.exception.RoomNotFoundException;
import com.mancalagame.domain.model.GameRoom;
import com.mancalagame.domain.model.Player;
import com.mancalagame.infrastructure.adapter.in.web.payload.CreateRoomRequest;
import com.mancalagame.infrastructure.adapter.in.web.payload.JoinRoomRequest;
import com.mancalagame.application.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- Import this

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*") // Allows your frontend to talk to this API during development
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // 1. POST /api/rooms/create
    @PostMapping("/create")
    public ResponseEntity<GameRoom> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        // Create the host player using the name from the JSON
        Player host = new Player(request.getPlayerName());

        // Let the Service build the room and the game engine
        GameRoom newRoom = roomService.createRoom(host);

        // Return the full room details (including the Room ID and Player UUID) back to the browser
        return ResponseEntity.ok(newRoom);
    }

    // In RoomController.java
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@Valid @RequestBody JoinRoomRequest request) {

            Player player2 = new Player(request.getPlayerName());
            GameRoom room = roomService.joinRoom(request.getRoomId(), player2);
            return ResponseEntity.ok(room);
    }

    // 3. GET /api/rooms/{roomId} - For reconnection
    @GetMapping("/{roomId}")
    public ResponseEntity<GameRoom> getRoom(@PathVariable String roomId) {
        GameRoom room = roomService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }
}