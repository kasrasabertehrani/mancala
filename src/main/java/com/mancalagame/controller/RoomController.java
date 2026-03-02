package com.mancalagame.controller;

import com.mancalagame.model.GameRoom;
import com.mancalagame.model.Player;
import com.mancalagame.payload.CreateRoomRequest;
import com.mancalagame.payload.JoinRoomRequest;
import com.mancalagame.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<GameRoom> createRoom(@RequestBody CreateRoomRequest request) {
        // Create the host player using the name from the JSON
        Player host = new Player(request.getPlayerName());

        // Let the Service build the room and the game engine
        GameRoom newRoom = roomService.createRoom(host);

        // Return the full room details (including the Room ID and Player UUID) back to the browser
        return ResponseEntity.ok(newRoom);
    }

    // 2. POST /api/rooms/join
    @PostMapping("/join")
    public ResponseEntity<GameRoom> joinRoom(@RequestBody JoinRoomRequest request) {
        // Create the second player
        Player player2 = new Player(request.getPlayerName());

        // Attempt to join the room
        GameRoom room = roomService.joinRoom(request.getRoomId(), player2);

        // If the room doesn't exist, or is already full, return a 400 Bad Request
        if (room == null || room.getGame().getPlayer2() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Return the successfully started game to the browser
        return ResponseEntity.ok(room);
    }
}