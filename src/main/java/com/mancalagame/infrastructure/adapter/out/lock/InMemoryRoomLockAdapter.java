package com.mancalagame.infrastructure.adapter.out.lock;

import com.mancalagame.application.port.out.RoomLockPort;
import com.mancalagame.domain.model.vo.RoomId;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRoomLockAdapter implements RoomLockPort {

    private final Map<RoomId, Object> roomLocks = new ConcurrentHashMap<>();

    @Override
    public <T> T executeWithLock(RoomId roomId, LockableOperation<T> operation) {
        Object lock = roomLocks.computeIfAbsent(roomId, k -> new Object());
        synchronized (lock) {
            return operation.execute();
        }
    }

    @Override
    public void releaseLock(RoomId roomId) {
        roomLocks.remove(roomId);
    }
}