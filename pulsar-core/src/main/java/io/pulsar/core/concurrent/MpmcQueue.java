package io.pulsar.core.concurrent;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * A sequenced, multi-producer multi-consumer (MPMC) queue based on a
 * disruptor-style
 * ring buffer with per-slot sequence numbers.
 *
 * @param <T> the element type
 */
public final class MpmcQueue<T> {

    private static final int SPIN_LIMIT = 100;

    private final AtomicReferenceArray<T> buffer;
    private final AtomicLongArray sequences;
    private final int mask;
    private final java.util.concurrent.atomic.AtomicLong head = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong tail = new java.util.concurrent.atomic.AtomicLong(0);

    public MpmcQueue(int capacity) {
        int cap = Integer.highestOneBit(capacity - 1) << 1;
        this.mask = cap - 1;
        this.buffer = new AtomicReferenceArray<>(cap);
        this.sequences = new AtomicLongArray(cap);
        for (int i = 0; i < cap; i++)
            sequences.set(i, i);
    }

    public boolean offer(T item) {
        long pos = tail.getAndIncrement();
        int slot = (int) (pos & mask);
        for (int spin = 0; sequences.get(slot) != pos; spin++) {
            if (spin > SPIN_LIMIT)
                Thread.yield();
        }
        buffer.set(slot, item);
        sequences.set(slot, pos + 1);
        return true;
    }

    public T poll() {
        long pos = head.getAndIncrement();
        int slot = (int) (pos & mask);
        for (int spin = 0; sequences.get(slot) != pos + 1; spin++) {
            if (spin > SPIN_LIMIT)
                Thread.yield();
        }
        T item = buffer.getAndSet(slot, null);
        sequences.set(slot, pos + mask + 1);
        return item;
    }

    public boolean isEmpty() {
        return head.get() >= tail.get();
    }
}
