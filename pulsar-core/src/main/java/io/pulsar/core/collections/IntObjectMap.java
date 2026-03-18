package io.pulsar.core.collections;

/**
 * A high-performance open-addressing hash map from {@code int} keys to
 * {@code Object} values.
 * Avoids boxing and reduces GC pressure compared to
 * {@code HashMap<Integer, V>}.
 *
 * @param <V> the value type
 */
@SuppressWarnings("unchecked")
public final class IntObjectMap<V> {

    private static final int EMPTY = Integer.MIN_VALUE;

    private int[] keys;
    private Object[] values;
    private int size;
    private int mask;

    public IntObjectMap(int initialCapacity) {
        int cap = nextPowerOfTwo(initialCapacity);
        this.keys = new int[cap];
        this.values = new Object[cap];
        this.mask = cap - 1;
        java.util.Arrays.fill(keys, EMPTY);
    }

    public void put(int key, V value) {
        int idx = hash(key) & mask;
        while (keys[idx] != EMPTY && keys[idx] != key) {
            idx = (idx + 1) & mask;
        }
        if (keys[idx] == EMPTY)
            size++;
        keys[idx] = key;
        values[idx] = value;
    }

    public V get(int key) {
        int idx = hash(key) & mask;
        while (keys[idx] != EMPTY) {
            if (keys[idx] == key)
                return (V) values[idx];
            idx = (idx + 1) & mask;
        }
        return null;
    }

    public boolean containsKey(int key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    private static int hash(int key) {
        key ^= (key >>> 16);
        key *= 0x45d9f3b;
        key ^= (key >>> 16);
        return key;
    }

    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n)
            p <<= 1;
        return p;
    }
}
