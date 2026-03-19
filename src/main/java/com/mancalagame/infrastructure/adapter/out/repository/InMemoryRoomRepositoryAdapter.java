package com.mancalagame.infrastructure.adapter.out.repository;

import com.mancalagame.application.port.out.GameRoomRepositoryPort;
import com.mancalagame.domain.model.GameRoom;
import com.mancalagame.infrastructure.SessionTracker;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRoomRepositoryAdapter implements GameRoomRepositoryPort {

    private final ConcurrentHashMap<String, GameRoom> activeRooms = new ConcurrentHashMap<>();
    private final SessionTracker sessionTracker;

    public InMemoryRoomRepositoryAdapter(SessionTracker sessionTracker) {
        this.sessionTracker = sessionTracker;
    }

    @Override
    public GameRoom findById(String roomId) { return activeRooms.get(roomId); }

    @Override
    public void save(GameRoom room) { activeRooms.put(room.getRoomId(), room); }

    @Override
    public void deleteById(String roomId) {
        activeRooms.remove(roomId);
        sessionTracker.removeSessionsByRoomId(roomId);
    }

    @Override
    public Collection<GameRoom> findAll() { return activeRooms.values(); }
}
