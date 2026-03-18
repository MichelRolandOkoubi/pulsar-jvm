package io.pulsar.core.buffer;

import io.pulsar.core.pool.PooledObject;
import java.nio.ByteBuffer;

/**
 * A pooled wrapper around a direct {@link ByteBuffer} that can be recycled.
 */
public final class PooledByteBuffer implements PooledObject {

    private final ByteBuffer buffer;
    private boolean inUse;

    public PooledByteBuffer(int capacity) {
        this.buffer = ByteBuffer.allocateDirect(capacity);
        this.inUse = false;
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void markInUse() {
        this.inUse = true;
    }

    @Override
    public void reset() {
        buffer.clear();
        inUse = false;
    }
}
