package io.pulsar.core.collections;

/**
 * Open-addressing primitive long-to-long hash map.
 */
public final class LongLongMap {

    private static final long EMPTY_KEY = Long.MIN_VALUE;
    private static final long NO_VALUE = Long.MIN_VALUE;

    private long[] keys;
    private long[] values;
    private int size;
    private int mask;

    public LongLongMap(int initialCapacity) {
        int cap = nextPowerOfTwo(initialCapacity);
        this.keys = new long[cap];
        this.values = new long[cap];
        this.mask = cap - 1;
        java.util.Arrays.fill(keys, EMPTY_KEY);
    }

    public void put(long key, long value) {
        int idx = hash(key) & mask;
        while (keys[idx] != EMPTY_KEY && keys[idx] != key)
            idx = (idx + 1) & mask;
        if (keys[idx] == EMPTY_KEY)
            size++;
        keys[idx] = key;
        values[idx] = value;
    }

    public long get(long key) {
        int idx = hash(key) & mask;
        while (keys[idx] != EMPTY_KEY) {
            if (keys[idx] == key)
                return values[idx];
            idx = (idx + 1) & mask;
        }
        return NO_VALUE;
    }

    public int size() {
        return size;
    }

    private static int hash(long k) {
        k = (k ^ (k >>> 30)) * 0xbf58476d1ce4e5b9L;
        return (int) (k ^ (k >>> 31));
    }

    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n)
            p <<= 1;
        return p;
    }
}
