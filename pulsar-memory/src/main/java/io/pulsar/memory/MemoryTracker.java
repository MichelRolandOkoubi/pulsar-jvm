package io.pulsar.memory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks native memory allocations for monitoring and debugging purposes.
 */
public final class MemoryTracker {

    private final AtomicLong allocatedBytes = new AtomicLong(0);
    private final AtomicLong allocationCount = new AtomicLong(0);
    private final AtomicLong freeCount = new AtomicLong(0);

    public void recordAllocation(long bytes) {
        allocatedBytes.addAndGet(bytes);
        allocationCount.incrementAndGet();
    }

    public void recordFree(long bytes) {
        allocatedBytes.addAndGet(-bytes);
        freeCount.incrementAndGet();
    }

    public long allocatedBytes() { return allocatedBytes.get(); }
    public long allocationCount() { return allocationCount.get(); }
    public long freeCount() { return freeCount.get(); }

    public void reset() {
        allocatedBytes.set(0);
        allocationCount.set(0);
        freeCount.set(0);
    }

    @Override
    public String toString() {
        return "MemoryTracker{allocatedBytes=" + allocatedBytes.get()
               + ", allocations=" + allocationCount.get()
               + ", frees=" + freeCount.get() + "}";
    }
}
