package io.pulsar.core.util;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Provides controlled access to {@link Unsafe} for off-heap operations.
 * Use with caution — direct memory operations bypass JVM safety checks.
 */
public final class UnsafeAccess {

    public static final Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private UnsafeAccess() {
    }

    public static long allocateMemory(long bytes) {
        return UNSAFE.allocateMemory(bytes);
    }

    public static void freeMemory(long address) {
        UNSAFE.freeMemory(address);
    }

    public static byte getByte(long address) {
        return UNSAFE.getByte(address);
    }

    public static void putByte(long address, byte value) {
        UNSAFE.putByte(address, value);
    }

    public static int getInt(long address) {
        return UNSAFE.getInt(address);
    }

    public static void putInt(long address, int value) {
        UNSAFE.putInt(address, value);
    }

    public static long getLong(long address) {
        return UNSAFE.getLong(address);
    }

    public static void putLong(long address, long value) {
        UNSAFE.putLong(address, value);
    }

    public static void copyMemory(long srcAddr, long dstAddr, long bytes) {
        UNSAFE.copyMemory(srcAddr, dstAddr, bytes);
    }

    public static ByteBuffer wrapAddress(long address, int capacity) {
        try {
            ByteBuffer bb = ByteBuffer.allocateDirect(0);
            Field addr = bb.getClass().getSuperclass().getDeclaredField("address");
            addr.setAccessible(true);
            addr.setLong(bb, address);
            Field cap = bb.getClass().getSuperclass().getDeclaredField("capacity");
            cap.setAccessible(true);
            cap.setInt(bb, capacity);
            bb.limit(capacity);
            return bb;
        } catch (Exception e) {
            throw new RuntimeException("Cannot wrap address as ByteBuffer", e);
        }
    }
}
