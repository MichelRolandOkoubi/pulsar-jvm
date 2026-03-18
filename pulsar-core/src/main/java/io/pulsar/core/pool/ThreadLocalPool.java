package io.pulsar.core.pool;

import java.util.function.Supplier;

/**
 * A zero-contention thread-local object pool.
 *
 * <p>
 * Unlike {@link ZeroAllocPool}, this pool stores objects in a
 * {@link ThreadLocal}
 * array, eliminating all CAS / volatile operations. This is the fastest
 * possible
 * pool when each thread has its own producer/consumer relationship with the
 * pool.
 * </p>
 *
 * <h3>Memory savings</h3>
 * Because each object is reused by the same thread with no synchronization
 * overhead,
 * you can safely keep the pool small (16-64 entries) without contention-based
 * spills.
 * This translates to far fewer live objects on the heap at any time.
 *
 * @param <T> the type of pooled object
 */
public final class ThreadLocalPool<T extends PooledObject> {

    private final ThreadLocal<Stack<T>> threadLocal;

    public ThreadLocalPool(int capacity, Supplier<T> factory) {
        this.threadLocal = ThreadLocal.withInitial(() -> new Stack<>(capacity, factory));
    }

    public T acquire() {
        return threadLocal.get().pop();
    }

    public void release(T obj) {
        obj.reset();
        threadLocal.get().push(obj);
    }

    /** Removes the thread-local stack (call from thread-pool shutdown hooks). */
    public void removeLocal() {
        threadLocal.remove();
    }

    // ---------------------------------------------------------- inner stack --

    @SuppressWarnings("unchecked")
    private static final class Stack<T extends PooledObject> {

        private final Object[] data;
        private final int mask;
        private int top;
        private final Supplier<T> factory;

        Stack(int capacity, Supplier<T> factory) {
            int cap = Integer.highestOneBit(capacity - 1) << 1;
            this.data = new Object[cap];
            this.mask = cap - 1;
            this.factory = factory;
            // pre-fill
            for (int i = 0; i < cap; i++)
                data[i] = factory.get();
            this.top = cap;
        }

        T pop() {
            if (top > 0)
                return (T) data[--top];
            return factory.get(); // fallback — no heap-shared contention
        }

        void push(T obj) {
            if (top <= mask)
                data[top++] = obj;
            // else discard — let GC collect the overflow; pool is healthy
        }
    }
}
