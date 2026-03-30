package com.mancalagame.application.port.out;

import com.mancalagame.domain.model.GameRoom;
import com.mancalagame.domain.model.vo.RoomId;

import java.util.Collection;
import java.util.Optional;

public interface GameRoomRepositoryPort {
    void save(GameRoom room);

    // DDD FIX: Use RoomId and Optional!
    Optional<GameRoom> findById(RoomId roomId);

    Collection<GameRoom> findAll();

    // DDD FIX: Use RoomId!
    void deleteById(RoomId roomId);
}