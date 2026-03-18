package io.pulsar.core.util;

/**
 * Bit-level utility methods for low-level performance work.
 */
public final class BitUtils {

    private BitUtils() {
    }

    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    public static int nextPowerOfTwo(int n) {
        if (isPowerOfTwo(n))
            return n;
        return Integer.highestOneBit(n) << 1;
    }

    public static int log2Floor(int n) {
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    public static int rotateLeft(int v, int dist) {
        return (v << dist) | (v >>> (32 - dist));
    }

    public static int rotateRight(int v, int dist) {
        return (v >>> dist) | (v << (32 - dist));
    }

    public static boolean isBitSet(long value, int bit) {
        return (value & (1L << bit)) != 0;
    }

    public static long setBit(long value, int bit) {
        return value | (1L << bit);
    }

    public static long clearBit(long value, int bit) {
        return value & ~(1L << bit);
    }
}
