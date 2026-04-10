package com.mancalagame.application.port.out;

import com.mancalagame.domain.model.vo.RoomId;

public interface RoomLockPort {


    <T> T executeWithLock(RoomId roomId, LockableOperation<T> operation);


    void releaseLock(RoomId roomId);

    @FunctionalInterface
    interface LockableOperation<T> {
        T execute();
    }
}