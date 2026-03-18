package io.pulsar.memory;

import io.pulsar.core.util.UnsafeAccess;

/**
 * An off-heap key-value map that stores fixed-size long-to-long entries.
 * Useful for large lookup tables that should not burden the GC.
 */
public final class OffHeapMap implements AutoCloseable {

    private static final long EMPTY_KEY = Long.MIN_VALUE;
    private static final int ENTRY_SIZE = 16; // 8 bytes key + 8 bytes value

    private final long base;
    private final int capacity;
    private final int mask;
    private int size;

    public OffHeapMap(int capacity) {
        int cap = nextPowerOfTwo(capacity);
        this.capacity = cap;
        this.mask = cap - 1;
        this.base = UnsafeAccess.allocateMemory((long) cap * ENTRY_SIZE);
        // Initialize all keys to EMPTY
        for (int i = 0; i < cap; i++) {
            UnsafeAccess.putLong(base + (long) i * ENTRY_SIZE, EMPTY_KEY);
        }
    }

    public void put(long key, long value) {
        int idx = hash(key) & mask;
        while (true) {
            long existingKey = UnsafeAccess.getLong(base + (long) idx * ENTRY_SIZE);
            if (existingKey == EMPTY_KEY || existingKey == key) {
                if (existingKey == EMPTY_KEY) size++;
                UnsafeAccess.putLong(base + (long) idx * ENTRY_SIZE, key);
                UnsafeAccess.putLong(base + (long) idx * ENTRY_SIZE + 8, value);
                return;
            }
            idx = (idx + 1) & mask;
        }
    }

    public long get(long key) {
        int idx = hash(key) & mask;
        while (true) {
            long k = UnsafeAccess.getLong(base + (long) idx * ENTRY_SIZE);
            if (k == EMPTY_KEY) return Long.MIN_VALUE;
            if (k == key) return UnsafeAccess.getLong(base + (long) idx * ENTRY_SIZE + 8);
            idx = (idx + 1) & mask;
        }
    }

    public int size() { return size; }

    @Override
    public void close() { UnsafeAccess.freeMemory(base); }

    private static int hash(long k) { return (int)((k ^ (k >>> 32)) * 0x9e3779b97f4a7c15L >>> 32); }
    private static int nextPowerOfTwo(int n) { int p=1; while(p<n) p<<=1; return p; }
}
