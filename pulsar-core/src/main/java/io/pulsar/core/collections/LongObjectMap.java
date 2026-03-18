package io.pulsar.core.collections;

/**
 * A high-performance open-addressing hash map from {@code long} keys to
 * {@code Object} values.
 *
 * @param <V> the value type
 */
@SuppressWarnings("unchecked")
public final class LongObjectMap<V> {

    private static final long EMPTY = Long.MIN_VALUE;

    private long[] keys;
    private Object[] values;
    private int size;
    private int mask;

    public LongObjectMap(int initialCapacity) {
        int cap = nextPowerOfTwo(initialCapacity);
        this.keys = new long[cap];
        this.values = new Object[cap];
        this.mask = cap - 1;
        java.util.Arrays.fill(keys, EMPTY);
    }

    public void put(long key, V value) {
        int idx = hash(key) & mask;
        while (keys[idx] != EMPTY && keys[idx] != key) {
            idx = (idx + 1) & mask;
        }
        if (keys[idx] == EMPTY)
            size++;
        keys[idx] = key;
        values[idx] = value;
    }

    public V get(long key) {
        int idx = hash(key) & mask;
        while (keys[idx] != EMPTY) {
            if (keys[idx] == key)
                return (V) values[idx];
            idx = (idx + 1) & mask;
        }
        return null;
    }

    public int size() {
        return size;
    }

    private static int hash(long key) {
        key = (key ^ (key >>> 30)) * 0xbf58476d1ce4e5b9L;
        key = (key ^ (key >>> 27)) * 0x94d049bb133111ebL;
        return (int) (key ^ (key >>> 31));
    }

    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n)
            p <<= 1;
        return p;
    }
}
