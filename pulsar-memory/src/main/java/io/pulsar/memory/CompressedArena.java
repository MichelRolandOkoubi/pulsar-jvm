package io.pulsar.memory;

import io.pulsar.core.util.UnsafeAccess;
import io.pulsar.core.util.MemoryUtils;

/**
 * A compressed arena that addresses memory via <b>32-bit offsets</b> rather than
 * 64-bit pointers, halving the per-reference overhead from 8 bytes to 4 bytes.
 *
 * <h3>Why this matters</h3>
 * In a standard 64-bit JVM, every object reference costs 8 bytes (with compressed
 * oops up to ~32 GB). When you maintain large in-memory tables of references,
 * the pointer array alone can consume tens of megabytes.
 *
 * <p>By constraining all allocations to a contiguous native region ≤ 4 GB,
 * we can represent any address as a 32-bit offset from the base. Arrays of
 * 32-bit int handles instead of 64-bit longs cut pointer table memory in half.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * try (CompressedArena arena = new CompressedArena(512 * 1024 * 1024)) { // 512 MB
 *     int handle = arena.allocate(64);          // 32-bit handle
 *     long addr  = arena.resolve(handle);       // real address when needed
 *     UnsafeAccess.putInt(addr, 42);
 * }
 * }</pre>
 */
public final class CompressedArena implements AutoCloseable {

    /** Maximum size: 4 GB (fits in unsigned 32-bit offset). */
    private static final long MAX_SIZE = 0xFFFFFFFFL;

    private final long base;
    private final long capacity;
    private long position;

    public CompressedArena(long capacityBytes) {
        if (capacityBytes > MAX_SIZE)
            throw new IllegalArgumentException("Capacity exceeds 4 GB limit for CompressedArena");
        this.capacity = capacityBytes;
        this.base     = UnsafeAccess.allocateMemory(capacityBytes);
        this.position = 0;
        MemoryUtils.zeroMemory(base, capacityBytes);
    }

    /**
     * Allocates {@code size} bytes and returns a <b>32-bit handle</b> (int offset).
     * Store this int in your data structures instead of a 64-bit pointer.
     *
     * @param size number of bytes (aligned up to 8)
     * @return unsigned 32-bit handle; use {@link #resolve(int)} to get the real address
     */
    public int allocate(int size) {
        long aligned = MemoryUtils.alignUp(position, 8);
        if (aligned + size > capacity)
            throw new OutOfMemoryError("CompressedArena capacity exceeded");
        int handle = (int) aligned;          // safe: capacity ≤ 4 GB
        position   = aligned + size;
        return handle;
    }

    /**
     * Resolves a 32-bit handle back to its 64-bit native address.
     * Only use this immediately before an Unsafe read/write — do NOT cache the result.
     */
    public long resolve(int handle) {
        return base + Integer.toUnsignedLong(handle);
    }

    /** Resets all allocations in O(1). */
    public void reset() { position = 0; }

    public long used()      { return position; }
    public long remaining() { return capacity - position; }
    public long base()      { return base; }

    @Override
    public void close() {
        UnsafeAccess.freeMemory(base);
    }
}
