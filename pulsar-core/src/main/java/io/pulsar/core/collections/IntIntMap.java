package io.pulsar.core.collections;

/**
 * Open-addressing primitive int-to-int hash map. No boxing, minimal GC impact.
 */
public final class IntIntMap {

    private static final int EMPTY_KEY = Integer.MIN_VALUE;
    private static final int NO_VALUE = Integer.MIN_VALUE;

    private int[] keys;
    private int[] values;
    private int size;
    private int mask;

    public IntIntMap(int initialCapacity) {
        int cap = nextPowerOfTwo(initialCapacity);
        this.keys = new int[cap];
        this.values = new int[cap];
        this.mask = cap - 1;
        java.util.Arrays.fill(keys, EMPTY_KEY);
    }

    public void put(int key, int value) {
        int idx = hash(key) & mask;
        while (keys[idx] != EMPTY_KEY && keys[idx] != key)
            idx = (idx + 1) & mask;
        if (keys[idx] == EMPTY_KEY)
            size++;
        keys[idx] = key;
        values[idx] = value;
    }

    public int get(int key) {
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

    private static int hash(int k) {
        k ^= k >>> 16;
        k *= 0x45d9f3b;
        k ^= k >>> 16;
        return k;
    }

    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n)
            p <<= 1;
        return p;
    }
}
