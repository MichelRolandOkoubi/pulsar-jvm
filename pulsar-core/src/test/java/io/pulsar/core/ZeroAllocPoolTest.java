package io.pulsar.core;

import io.pulsar.core.pool.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ZeroAllocPoolTest {

    static class TestObject implements PooledObject {
        boolean reset = false;

        @Override
        public void reset() {
            reset = true;
        }
    }

    private ZeroAllocPool<TestObject> pool;

    @BeforeEach
    void setUp() {
        pool = new ZeroAllocPool<>(PoolConfig.of(16), TestObject::new);
    }

    @Test
    void acquireReturnsObject() {
        TestObject obj = pool.acquire();
        assertNotNull(obj);
    }

    @Test
    void releaseAndReacquire() {
        TestObject obj = pool.acquire();
        pool.release(obj);
        assertTrue(obj.reset, "reset() should have been called on release");
    }
}
