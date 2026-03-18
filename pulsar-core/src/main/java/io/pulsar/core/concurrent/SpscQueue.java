package io.pulsar.core.concurrent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Single-Producer Single-Consumer (SPSC) queue — highly optimized for the
 * common
 * producer/consumer pipeline pattern. Uses less overhead than MPMC.
 *
 * @param <T> the element type
 */
public final class SpscQueue<T> {

    private final AtomicReferenceArray<T> buffer;
    private final int mask;
    private long producerIndex = 0;
    private long consumerIndex = 0;

    public SpscQueue(int capacity) {
        int cap = Integer.highestOneBit(capacity - 1) << 1;
        this.mask = cap - 1;
        this.buffer = new AtomicReferenceArray<>(cap);
    }

    public boolean offer(T item) {
        if (((producerIndex - consumerIndex) & ~mask) != 0)
            return false;
        buffer.lazySet((int) (producerIndex & mask), item);
        producerIndex++;
        return true;
    }

    public T poll() {
        T item = buffer.get((int) (consumerIndex & mask));
        if (item == null)
            return null;
        buffer.lazySet((int) (consumerIndex & mask), null);
        consumerIndex++;
        return item;
    }

    public boolean isEmpty() {
        return producerIndex == consumerIndex;
    }
}
