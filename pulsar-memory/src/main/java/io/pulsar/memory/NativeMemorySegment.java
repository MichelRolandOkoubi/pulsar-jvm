package io.pulsar.memory;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * A wrapper around Java 21 Panama {@link MemorySegment} providing a safe,
 * structured alternative to {@code sun.misc.Unsafe} for off-heap memory.
 *
 * <h3>Why Panama over Unsafe?</h3>
 * <ul>
 *   <li>No {@code --add-exports} JVM flags required.</li>
 *   <li>Bounds-checked by default (can be disabled with {@link Arena#global()}).</li>
 *   <li>Interoperable with {@code VarHandle} and {@code MethodHandle} for
 *       zero-overhead field access.</li>
 *   <li>Native arrays become first-class, allowing SIMD-like vectorised access
 *       without reflection hacks.</li>
 * </ul>
 *
 * <h3>Memory savings</h3>
 * Because all data lives in native memory, the JVM heap only contains a thin
 * {@link MemorySegment} handle (~40 bytes) per allocation instead of the full
 * object graph. For 10 000 objects of 64 bytes each you save ≈ 620 KB of heap.
 */
public final class NativeMemorySegment implements AutoCloseable {

    private static final ValueLayout.OfByte   BYTE_LAYOUT   = ValueLayout.JAVA_BYTE;
    private static final ValueLayout.OfInt    INT_LAYOUT    = ValueLayout.JAVA_INT.withOrder(ByteOrder.nativeOrder());
    private static final ValueLayout.OfLong   LONG_LAYOUT   = ValueLayout.JAVA_LONG.withOrder(ByteOrder.nativeOrder());
    private static final ValueLayout.OfDouble DOUBLE_LAYOUT = ValueLayout.JAVA_DOUBLE.withOrder(ByteOrder.nativeOrder());

    private final Arena         arena;
    private final MemorySegment segment;

    /** Allocates a new confined (thread-safe checked) off-heap segment. */
    public NativeMemorySegment(long byteSize) {
        this.arena   = Arena.ofConfined();
        this.segment = arena.allocate(byteSize, 8);
    }

    /** Wraps an existing segment (e.g. from memory-mapped files). */
    public NativeMemorySegment(MemorySegment segment) {
        this.arena   = null;
        this.segment = segment;
    }

    // -------------------------------------------------- primitive accessors --

    public byte   getByte(long offset)           { return segment.get(BYTE_LAYOUT,   offset); }
    public void   putByte(long offset, byte v)   { segment.set(BYTE_LAYOUT,   offset, v); }

    public int    getInt(long offset)            { return segment.get(INT_LAYOUT,    offset); }
    public void   putInt(long offset, int v)     { segment.set(INT_LAYOUT,    offset, v); }

    public long   getLong(long offset)           { return segment.get(LONG_LAYOUT,   offset); }
    public void   putLong(long offset, long v)   { segment.set(LONG_LAYOUT,   offset, v); }

    public double getDouble(long offset)         { return segment.get(DOUBLE_LAYOUT, offset); }
    public void   putDouble(long offset, double v) { segment.set(DOUBLE_LAYOUT, offset, v); }

    /** Copies bytes from a heap byte array into native memory (zero-copy direction). */
    public void copyFromArray(byte[] src, int srcOffset, long dstOffset, int length) {
        MemorySegment.copy(MemorySegment.ofArray(src), srcOffset, segment, dstOffset, length);
    }

    /** Copies bytes from native memory to a heap byte array. */
    public void copyToArray(long srcOffset, byte[] dst, int dstOffset, int length) {
        MemorySegment.copy(segment, srcOffset, MemorySegment.ofArray(dst), dstOffset, length);
    }

    /** Zero out the entire segment. */
    public void zero() { segment.fill((byte) 0); }

    public long   byteSize() { return segment.byteSize(); }
    public MemorySegment rawSegment() { return segment; }

    @Override
    public void close() {
        if (arena != null) arena.close();
    }
}
