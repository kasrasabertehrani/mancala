package com.mancalagame.infrastructure.adapter.out.repository;

import com.mancalagame.application.port.out.GameRoomRepositoryPort;
import com.mancalagame.domain.model.GameRoom;
import com.mancalagame.domain.model.vo.RoomId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRoomRepositoryAdapter implements GameRoomRepositoryPort {

    // The internal map still uses String for the key
    private final ConcurrentHashMap<String, GameRoom> rooms = new ConcurrentHashMap<>();

    @Override
    public void save(GameRoom room) {
        // Extract the raw string value to use as the map key
        rooms.put(room.getRoomId().value(), room);
    }

    @Override
    public Optional<GameRoom> findById(RoomId roomId) {
        // Wrap the result in an Optional, extracting the string value for the lookup
        return Optional.ofNullable(rooms.get(roomId.value()));
    }

    @Override
    public Collection<GameRoom> findAll() {
        return rooms.values();
    }

    @Override
    public void deleteById(RoomId roomId) {
        // Extract the string value to remove it
        rooms.remove(roomId.value());
    }
}