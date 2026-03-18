package io.pulsar.memory;

import io.pulsar.core.util.UnsafeAccess;
import io.pulsar.core.util.MemoryUtils;

/**
 * A fully off-heap open-addressing hash map with fixed-size value structs.
 *
 * <h3>Layout per slot (native memory)</h3>
 * <pre>
 *   [0..7]       key   (long)
 *   [8..8+VS-1]  value (VS bytes, caller-defined struct)
 * </pre>
 *
 * <h3>Memory saving</h3>
 * A {@code HashMap<Long, MyStruct>}:
 * <ul>
 *   <li>Entry object: 32 bytes minimum</li>
 *   <li>Long key box: 24 bytes</li>
 *   <li>MyStruct: 16-200 bytes + header</li>
 * </ul>
 * {@code OffHeapHashMap} with 24-byte value struct: <b>8 + 24 = 32 bytes/slot</b>
 * in native memory, no heap objects at all. At 1M entries:
 * <pre>
 *   HashMap    : 1M × (32 + 24 + 24) = 80 MB heap
 *   OffHeapHashMap : 1M × 32 = 32 MB native  (-60%, zero GC pressure)
 * </pre>
 *
 * @param valueSize byte size of each value struct (fixed, must be multiple of 8)
 */
public final class OffHeapHashMap implements AutoCloseable {

    private static final long EMPTY_KEY   = Long.MIN_VALUE;
    private static final long TOMBSTONE   = Long.MIN_VALUE + 1;

    private final int  valueSize;
    private final int  slotSize;      // 8 (key) + valueSize
    private final int  capacity;      // must be power of 2
    private final int  mask;
    private final long base;
    private int        size;

    public OffHeapHashMap(int capacity, int valueSize) {
        if (valueSize % 8 != 0) throw new IllegalArgumentException("valueSize must be multiple of 8");
        int cap      = Integer.highestOneBit(capacity - 1) << 1;
        this.capacity  = cap;
        this.mask      = cap - 1;
        this.valueSize = valueSize;
        this.slotSize  = 8 + valueSize;
        this.base      = UnsafeAccess.allocateMemory((long) cap * slotSize);
        // Mark all slots empty
        for (int i = 0; i < cap; i++)
            UnsafeAccess.putLong(base + (long) i * slotSize, EMPTY_KEY);
    }

    /**
     * Returns the native address of the value struct for {@code key},
     * creating a new slot if necessary. Caller writes to the returned address.
     * Returns 0L if the map is full.
     */
    public long getOrCreate(long key) {
        int idx = hash(key) & mask;
        int firstTombstone = -1;
        for (int probe = 0; probe < capacity; probe++) {
            long slotAddr = base + (long) idx * slotSize;
            long k        = UnsafeAccess.getLong(slotAddr);
            if (k == EMPTY_KEY) {
                if (firstTombstone >= 0) {
                    slotAddr = base + (long) firstTombstone * slotSize;
                    idx = firstTombstone;
                }
                UnsafeAccess.putLong(slotAddr, key);
                size++;
                return slotAddr + 8;
            }
            if (k == key) return slotAddr + 8;
            if (k == TOMBSTONE && firstTombstone < 0) firstTombstone = idx;
            idx = (idx + 1) & mask;
        }
        return 0L; // full
    }

    /**
     * Returns the native address of the value struct for {@code key}, or 0L if absent.
     */
    public long get(long key) {
        int idx = hash(key) & mask;
        for (int probe = 0; probe < capacity; probe++) {
            long slotAddr = base + (long) idx * slotSize;
            long k        = UnsafeAccess.getLong(slotAddr);
            if (k == EMPTY_KEY)   return 0L;
            if (k == key)         return slotAddr + 8;
            idx = (idx + 1) & mask;
        }
        return 0L;
    }

    /** Removes a key. Marks slot as tombstone. Returns true if found. */
    public boolean remove(long key) {
        int idx = hash(key) & mask;
        for (int probe = 0; probe < capacity; probe++) {
            long slotAddr = base + (long) idx * slotSize;
            long k        = UnsafeAccess.getLong(slotAddr);
            if (k == EMPTY_KEY)   return false;
            if (k == key) {
                UnsafeAccess.putLong(slotAddr, TOMBSTONE);
                size--;
                return true;
            }
            idx = (idx + 1) & mask;
        }
        return false;
    }

    public int  size()      { return size;     }
    public int  capacity()  { return capacity; }
    public int  valueSize() { return valueSize; }

    private static int hash(long key) {
        key ^= (key >>> 33);
        key *= 0xff51afd7ed558ccdL;
        key ^= (key >>> 33);
        return (int) key;
    }

    @Override
    public void close() { UnsafeAccess.freeMemory(base); }
}
