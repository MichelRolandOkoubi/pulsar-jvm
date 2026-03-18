package io.pulsar.memory;

import java.util.function.Supplier;

/**
 * A flyweight factory that pre-creates shareable, stateless or resettable instances
 * and provides thread-local access for zero-allocation patterns.
 *
 * @param <T> the flyweight type
 */
public final class FlyweightFactory<T> {

    private final ThreadLocal<T> threadLocal;

    public FlyweightFactory(Supplier<T> supplier) {
        this.threadLocal = ThreadLocal.withInitial(supplier);
    }

    /**
     * Returns the thread-local flyweight instance.
     */
    public T get() { return threadLocal.get(); }

    /**
     * Removes the thread-local instance (useful for cleanup in thread pools).
     */
    public void remove() { threadLocal.remove(); }
}
