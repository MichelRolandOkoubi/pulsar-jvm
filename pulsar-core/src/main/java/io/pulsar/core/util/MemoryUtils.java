package io.pulsar.core.util;

/**
 * Memory utility helpers for alignment, copying, and zeroing native memory.
 */
public final class MemoryUtils {

    private MemoryUtils() {
    }

    /**
     * Aligns the given value up to the specified alignment (must be power of 2).
     */
    public static long alignUp(long value, int alignment) {
        return (value + alignment - 1) & ~(alignment - 1L);
    }

    /**
     * Zero out a native memory region.
     */
    public static void zeroMemory(long address, long bytes) {
        UnsafeAccess.UNSAFE.setMemory(address, bytes, (byte) 0);
    }

    /**
     * Copy native memory from src to dst.
     */
    public static void copyMemory(long src, long dst, long bytes) {
        UnsafeAccess.copyMemory(src, dst, bytes);
    }

    /**
     * Returns the cache-line size (typically 64 bytes on x86).
     */
    public static int cacheLineSize() {
        return 64;
    }
}
