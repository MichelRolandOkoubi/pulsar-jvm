package io.pulsar.core.pool;

/**
 * Generic object pool interface for managing reusable objects.
 *
 * @param <T> the type of pooled object
 */
public interface ObjectPool<T extends PooledObject> {

    /**
     * Acquires an object from the pool. If the pool is empty, a new object is created.
     *
     * @return a recycled or newly created object
     */
    T acquire();

    /**
     * Returns an object to the pool for future reuse.
     *
     * @param obj the object to release
     */
    void release(T obj);
}
