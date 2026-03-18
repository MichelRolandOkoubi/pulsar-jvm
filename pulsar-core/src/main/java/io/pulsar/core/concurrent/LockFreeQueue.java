package io.pulsar.core.concurrent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A lock-free, bounded, multi-producer multi-consumer (MPMC) queue.
 * Based on a circular buffer with sequenced slots.
 *
 * @param <T> the element type
 */
public final class LockFreeQueue<T> {

    private static final int BUFFER_PAD = 32;

    private final AtomicReferenceArray<T> buffer;
    private final int mask;
    private final AtomicLong head = new AtomicLong(0);
    private final AtomicLong tail = new AtomicLong(0);

    public LockFreeQueue(int capacity) {
        int cap = nextPowerOfTwo(capacity);
        this.mask = cap - 1;
        this.buffer = new AtomicReferenceArray<>(cap);
    }

    public boolean offer(T item) {
        long t;
        do {
            t = tail.get();
            if (t - head.get() >= (mask + 1))
                return false; // full
        } while (!tail.compareAndSet(t, t + 1));
        buffer.set((int) (t & mask), item);
        return true;
    }

    public T poll() {
        long h;
        do {
            h = head.get();
            if (h >= tail.get())
                return null; // empty
        } while (!head.compareAndSet(h, h + 1));
        T item = buffer.getAndSet((int) (h & mask), null);
        return item;
    }

    public boolean isEmpty() {
        return head.get() >= tail.get();
    }

    public int size() {
        return (int) (tail.get() - head.get());
    }

    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n)
            p <<= 1;
        return p;
    }
}
