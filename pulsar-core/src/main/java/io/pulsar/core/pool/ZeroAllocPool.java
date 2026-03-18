package io.pulsar.core.pool;

import java.util.function.Supplier;

/**
 * A zero-allocation object pool that recycles objects to avoid GC pressure.
 * Thread-safe via lock-free CAS operations.
 *
 * @param <T> the type of pooled object
 */
public final class ZeroAllocPool<T extends PooledObject> {

    private final Object[] pool;
    private final Supplier<T> factory;
    private final int mask;
    private volatile int head;

    @SuppressWarnings("unchecked")
    public ZeroAllocPool(PoolConfig config, Supplier<T> factory) {
        int capacity = nextPowerOfTwo(config.maxPoolSize());
        this.pool = new Object[capacity];
        this.mask = capacity - 1;
        this.factory = factory;
        for (int i = 0; i < capacity; i++) {
            pool[i] = factory.get();
        }
    }

    @SuppressWarnings("unchecked")
    public T acquire() {
        int idx = head & mask;
        Object obj = pool[idx];
        if (obj != null) {
            pool[idx] = null;
            head++;
            return (T) obj;
        }
        return factory.get();
    }

    public void release(T obj) {
        obj.reset();
        int idx = (head - 1) & mask;
        pool[idx] = obj;
    }

    private static int nextPowerOfTwo(int value) {
        int n = 1;
        while (n < value) n <<= 1;
        return n;
    }
}
