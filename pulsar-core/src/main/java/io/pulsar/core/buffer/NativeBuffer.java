package io.pulsar.core.buffer;

import io.pulsar.core.util.UnsafeAccess;
import java.nio.ByteBuffer;

/**
 * A native (off-heap) buffer backed by direct memory.
 * Provides zero-copy access and avoids garbage collection overhead.
 */
public final class NativeBuffer implements AutoCloseable {

    private long address;
    private final int capacity;
    private int position;
    private boolean freed;

    public NativeBuffer(int capacity) {
        this.capacity = capacity;
        this.address = UnsafeAccess.allocateMemory(capacity);
        this.position = 0;
        this.freed = false;
    }

    public long address() {
        return address;
    }

    public int capacity() {
        return capacity;
    }

    public int position() {
        return position;
    }

    public int remaining() {
        return capacity - position;
    }

    public byte readByte() {
        checkBounds(1);
        return UnsafeAccess.getByte(address + position++);
    }

    public void writeByte(byte b) {
        checkBounds(1);
        UnsafeAccess.putByte(address + position++, b);
    }

    public int readInt() {
        checkBounds(4);
        int val = UnsafeAccess.getInt(address + position);
        position += 4;
        return val;
    }

    public void writeInt(int value) {
        checkBounds(4);
        UnsafeAccess.putInt(address + position, value);
        position += 4;
    }

    public void reset() {
        position = 0;
    }

    public ByteBuffer asByteBuffer() {
        return UnsafeAccess.wrapAddress(address, capacity);
    }

    private void checkBounds(int bytes) {
        if (position + bytes > capacity) {
            throw new IndexOutOfBoundsException("Buffer overflow: position=" + position + " capacity=" + capacity);
        }
    }

    @Override
    public void close() {
        if (!freed) {
            UnsafeAccess.freeMemory(address);
            freed = true;
            address = 0;
        }
    }
}
