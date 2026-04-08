package com.mancalagame.application.port.out;

import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.RoomId;

import java.util.Collection;
import java.util.Optional;

public interface RoomRepositoryPort {

    void save(Room room);

    Optional<Room> findById(RoomId roomId);

    Collection<Room> findAll();

    void deleteById(RoomId roomId);
}