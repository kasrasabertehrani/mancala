package com.mancalagame.application.port.out;

import com.mancalagame.domain.model.GameRoom;
import java.util.Collection;

public interface GameRoomRepositoryPort {
    GameRoom findById(String roomId);
    void save(GameRoom room);
    void deleteById(String roomId);
    Collection<GameRoom> findAll();
}