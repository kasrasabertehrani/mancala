package com.mancalagame.infrastructure.adapter.out.repository;

import com.mancalagame.application.port.out.RoomRepositoryPort;
import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.RoomId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class InMemoryRoomRepositoryAdapter implements RoomRepositoryPort {

    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

    @Override
    public void save(Room room) {
        rooms.put(room.getRoomId().value(), room);
    }

    @Override
    public Optional<Room> findById(RoomId roomId) {
        return Optional.ofNullable(rooms.get(roomId.value()));
    }

    @Override
    public Collection<Room> findAll() {
        return rooms.values();
    }

    @Override
    public void deleteById(RoomId roomId) {
        rooms.remove(roomId.value());
    }
}