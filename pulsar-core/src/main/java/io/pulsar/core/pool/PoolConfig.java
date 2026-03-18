package io.pulsar.core.pool;

/**
 * Configuration for an object pool.
 *
 * @param maxPoolSize maximum number of objects in the pool
 * @param preallocate whether to pre-allocate objects at startup
 * @param threadLocal whether to use thread-local pools for additional
 *                    performance
 */
public record PoolConfig(
        int maxPoolSize,
        boolean preallocate,
        boolean threadLocal) {
    public static final PoolConfig DEFAULT = new PoolConfig(256, true, false);

    public PoolConfig {
        if (maxPoolSize <= 0)
            throw new IllegalArgumentException("maxPoolSize must be > 0");
    }

    public static PoolConfig of(int maxPoolSize) {
        return new PoolConfig(maxPoolSize, true, false);
    }
}
