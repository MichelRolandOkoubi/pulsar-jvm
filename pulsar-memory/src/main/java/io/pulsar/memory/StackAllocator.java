package io.pulsar.memory;

import io.pulsar.core.util.UnsafeAccess;
import io.pulsar.core.util.MemoryUtils;

/**
 * A native stack (LIFO) allocator. Push allocations onto the stack, and pop (free) them
 * in reverse order. Ideal for temporary scoped allocations.
 */
public final class StackAllocator implements AutoCloseable {

    private final long base;
    private final long capacity;
    private long top;

    public StackAllocator(long capacity) {
        this.capacity = capacity;
        this.base = UnsafeAccess.allocateMemory(capacity);
        this.top = base;
    }

    public long push(int size) {
        long aligned = MemoryUtils.alignUp(top - base, 8) + base;
        if (aligned + size > base + capacity) {
            throw new OutOfMemoryError("StackAllocator overflow");
        }
        long addr = aligned;
        top = aligned + size;
        return addr;
    }

    public void pop(long mark) {
        if (mark < base || mark > top) {
            throw new IllegalArgumentException("Invalid mark for StackAllocator pop");
        }
        top = mark;
    }

    public long mark() { return top; }
    public long used() { return top - base; }

    @Override
    public void close() { UnsafeAccess.freeMemory(base); }
}
