package com.mancalagame.infrastructure.adapter.out.lock;

import com.mancalagame.domain.model.vo.RoomId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRoomLockAdapterTest {

    private InMemoryRoomLockAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryRoomLockAdapter();
    }

    @Test
    void executeWithLock_shouldReturnOperationResult() {
        RoomId roomId = new RoomId("room-1");

        String result = adapter.executeWithLock(roomId, () -> "ok");

        assertEquals("ok", result);
    }

    @Test
    void executeWithLock_shouldPropagateOperationException() {
        RoomId roomId = new RoomId("room-1");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adapter.executeWithLock(roomId, () -> {
                    throw new RuntimeException("boom");
                }));

        assertEquals("boom", ex.getMessage());
    }

    @Test
    void executeWithLock_shouldMutuallyExcludeOperationsForSameRoom() throws Exception {
        RoomId roomId = new RoomId("same-room");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger activeInCriticalSection = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);

        Callable<Void> task = () -> {
            startGate.await();
            adapter.executeWithLock(roomId, () -> {
                int nowActive = activeInCriticalSection.incrementAndGet();
                maxConcurrent.updateAndGet(prev -> Math.max(prev, nowActive));
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Unexpected interruption");
                } finally {
                    activeInCriticalSection.decrementAndGet();
                }
                return null;
            });
            return null;
        };

        Future<Void> f1 = pool.submit(task);
        Future<Void> f2 = pool.submit(task);
        startGate.countDown();

        f1.get(2, TimeUnit.SECONDS);
        f2.get(2, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertEquals(1, maxConcurrent.get());
    }

    @Test
    void executeWithLock_shouldAllowParallelOperationsForDifferentRooms() throws Exception {
        RoomId room1 = new RoomId("room-1");
        RoomId room2 = new RoomId("room-2");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        CyclicBarrier bothInsideBarrier = new CyclicBarrier(2);

        Callable<Void> task1 = () -> {
            startGate.await();
            adapter.executeWithLock(room1, () -> {
                awaitBarrier(bothInsideBarrier);
                return null;
            });
            return null;
        };

        Callable<Void> task2 = () -> {
            startGate.await();
            adapter.executeWithLock(room2, () -> {
                awaitBarrier(bothInsideBarrier);
                return null;
            });
            return null;
        };

        Future<Void> f1 = pool.submit(task1);
        Future<Void> f2 = pool.submit(task2);
        startGate.countDown();

        f1.get(2, TimeUnit.SECONDS);
        f2.get(2, TimeUnit.SECONDS);
        pool.shutdownNow();
    }

    @Test
    void releaseLock_shouldRemoveRoomLockAndBeIdempotent() throws Exception {
        RoomId roomId = new RoomId("room-1");

        adapter.executeWithLock(roomId, () -> null);

        Map<RoomId, Object> lockMap = getRoomLocks(adapter);
        assertTrue(lockMap.containsKey(roomId));

        adapter.releaseLock(roomId);
        assertFalse(lockMap.containsKey(roomId));

        assertDoesNotThrow(() -> adapter.releaseLock(roomId));
    }

    private static void awaitBarrier(CyclicBarrier barrier) {
        try {
            barrier.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (BrokenBarrierException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<RoomId, Object> getRoomLocks(InMemoryRoomLockAdapter adapter) throws Exception {
        Field field = InMemoryRoomLockAdapter.class.getDeclaredField("roomLocks");
        field.setAccessible(true);
        return (Map<RoomId, Object>) field.get(adapter);
    }
}
