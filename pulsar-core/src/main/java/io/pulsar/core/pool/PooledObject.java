package io.pulsar.core.pool;

/**
 * Marker interface for objects that can be pooled.
 * Implementations must provide a {@code reset()} method to clear state before reuse.
 */
public interface PooledObject {

    /**
     * Resets this object to its initial state so it can be safely reused.
     */
    void reset();
}
