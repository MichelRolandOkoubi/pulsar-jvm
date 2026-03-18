package io.pulsar.memory;

import io.pulsar.core.util.MemoryUtils;
import io.pulsar.core.util.UnsafeAccess;

/**
 * A bump-pointer arena allocator over a pre-allocated native memory region.
 * All allocations are O(1) and freed together when the arena is reset or closed.
 *
 * <p>This pattern is ideal for request-scoped allocations where all objects share the same lifetime.</p>
 */
public final class ArenaAllocator implements AutoCloseable {

    private final long base;
    private final long capacity;
    private long position;

    public ArenaAllocator(long capacity) {
        this.capacity = capacity;
        this.base = UnsafeAccess.allocateMemory(capacity);
        this.position = 0;
        MemoryUtils.zeroMemory(base, capacity);
    }

    /**
     * Allocates {@code size} bytes from this arena, aligned to 8 bytes.
     *
     * @param size number of bytes to allocate
     * @return address of the allocated region
     * @throws OutOfMemoryError if the arena is full
     */
    public long allocate(int size) {
        long aligned = MemoryUtils.alignUp(position, 8);
        if (aligned + size > capacity) {
            throw new OutOfMemoryError("ArenaAllocator capacity exceeded: capacity=" + capacity);
        }
        position = aligned + size;
        return base + aligned;
    }

    /** Resets the arena, making all previously allocated memory available again. */
    public void reset() { position = 0; }

    public long used() { return position; }
    public long remaining() { return capacity - position; }

    @Override
    public void close() {
        UnsafeAccess.freeMemory(base);
    }
}
