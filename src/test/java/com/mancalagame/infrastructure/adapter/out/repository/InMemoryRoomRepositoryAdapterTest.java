package com.mancalagame.infrastructure.adapter.out.repository;

import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.RoomId;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryRoomRepositoryAdapterTest {

    @Test
    void shouldSaveAndFindRoom() {
        InMemoryRoomRepositoryAdapter repository = new InMemoryRoomRepositoryAdapter();
        RoomId roomId = new RoomId("1");
        Player playerOne = new Player("Alice");
        Room room = new Room(roomId, playerOne);

        repository.save(room);
        assertEquals(room, repository.findById(roomId).orElse(null));
    }

    @Test
    void shouldFindAll() {
        InMemoryRoomRepositoryAdapter repository = new InMemoryRoomRepositoryAdapter();
        RoomId roomOneId = new RoomId("1");
        RoomId roomTwoId = new RoomId("2");
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Room roomOne = new Room(roomOneId, playerOne);
        Room roomTwo = new Room(roomTwoId, playerTwo);

        repository.save(roomOne);
        repository.save(roomTwo);
        Iterable<Room> rooms = repository.findAll();
        assertEquals(2, ((Collection<Room>) rooms).size());
    }

    @Test
    void shouldDeleteById() {
        InMemoryRoomRepositoryAdapter repository = new InMemoryRoomRepositoryAdapter();
        RoomId roomId = new RoomId("1");
        Player playerOne = new Player("Alice");
        Room room = new Room(roomId, playerOne);

        repository.save(room);
        repository.deleteById(roomId);

        assertEquals(0, ((Collection<Room>) repository.findAll()).size());
    }
}
