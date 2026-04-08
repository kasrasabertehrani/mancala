package com.mancalagame.infrastructure.adapter.in.web;

import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.Player;
import com.mancalagame.infrastructure.adapter.in.web.payload.CreateRoomRequest;
import com.mancalagame.infrastructure.adapter.in.web.payload.JoinRoomRequest;
import com.mancalagame.application.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Player host = new Player(request.getPlayerName());
        Room newRoom = roomService.createRoom(host);
        return ResponseEntity.ok(newRoom);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@Valid @RequestBody JoinRoomRequest request) {
            Player player2 = new Player(request.getPlayerName());
            Room room = roomService.joinRoom(request.getRoomId(), player2);
            return ResponseEntity.ok(room);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomId) {
        Room room = roomService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }
}