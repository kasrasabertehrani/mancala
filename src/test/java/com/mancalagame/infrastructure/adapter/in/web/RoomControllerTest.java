package com.mancalagame.infrastructure.adapter.in.web;

import com.mancalagame.application.port.in.RoomUseCase;
import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.RoomId;
import com.mancalagame.infrastructure.adapter.in.web.payload.request.CreateRoomRequest;
import com.mancalagame.infrastructure.adapter.in.web.payload.request.JoinRoomRequest;
import com.mancalagame.infrastructure.adapter.in.web.payload.response.RoomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    private RoomUseCase roomService;
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomUseCase.class);
        roomController = new RoomController(roomService);
    }

    @Test
    void createRoom_shouldReturnCreatedResponseAndCallServiceWithHostPlayer() {
        CreateRoomRequest request = mock(CreateRoomRequest.class);
        when(request.getPlayerName()).thenReturn("Alice");

        Room createdRoom = buildRoom("1", "Alice");
        when(roomService.createRoom(any(Player.class))).thenReturn(createdRoom);

        var response = roomController.createRoom(request);

        assertEquals(201, response.getStatusCode().value());
        RoomResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("1", body.getRoomId());
        assertEquals("WAITING_FOR_PLAYER_2", body.getGame().getGameStatus());

        ArgumentCaptor<Player> hostCaptor = ArgumentCaptor.forClass(Player.class);
        verify(roomService).createRoom(hostCaptor.capture());
        assertEquals("Alice", hostCaptor.getValue().getName());
    }

    @Test
    void joinRoom_shouldReturnOkResponseAndCallServiceWithRoomIdAndPlayer() {
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomId("1");
        request.setPlayerName("Bob");

        Room room = buildRoom("1", "Alice");
        room.addPlayer(new Player("Bob"));
        when(roomService.joinRoom(eq("1"), any(Player.class))).thenReturn(room);

        var response = roomController.joinRoom(request);

        assertEquals(200, response.getStatusCode().value());
        RoomResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("1", body.getRoomId());
        assertEquals("PLAYER_1_TURN", body.getGame().getGameStatus());

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(roomService).joinRoom(eq("1"), playerCaptor.capture());
        assertEquals("Bob", playerCaptor.getValue().getName());
    }

    @Test
    void getRoom_shouldReturnOkResponseAndCallService() {
        Room room = buildRoom("55", "Alice");
        when(roomService.getRoom("55")).thenReturn(room);

        var response = roomController.getRoom("55");

        assertEquals(200, response.getStatusCode().value());
        RoomResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("55", body.getRoomId());

        verify(roomService).getRoom("55");
    }

    private static Room buildRoom(String roomId, String hostName) {
        return new Room(new RoomId(roomId), new Player(hostName));
    }
}

