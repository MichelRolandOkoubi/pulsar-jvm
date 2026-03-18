package io.pulsar.core.collections;

/**
 * Factory utility for creating primitive collections with sensible defaults.
 */
public final class PrimitiveCollections {

    private PrimitiveCollections() {
    }

    public static <V> IntObjectMap<V> intObjectMap(int initialCapacity) {
        return new IntObjectMap<>(initialCapacity);
    }

    public static <V> LongObjectMap<V> longObjectMap(int initialCapacity) {
        return new LongObjectMap<>(initialCapacity);
    }

    public static IntIntMap intIntMap(int initialCapacity) {
        return new IntIntMap(initialCapacity);
    }

    public static LongLongMap longLongMap(int initialCapacity) {
        return new LongLongMap(initialCapacity);
    }
}
