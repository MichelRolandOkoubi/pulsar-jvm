package io.pulsar.core.buffer;

import io.pulsar.core.pool.PoolConfig;
import io.pulsar.core.pool.ZeroAllocPool;

/**
 * A pool of direct {@link PooledByteBuffer} instances to avoid repeated
 * allocation
 * of direct memory.
 */
public final class DirectBufferPool {

    private final ZeroAllocPool<PooledByteBuffer> pool;
    private final int bufferSize;

    public DirectBufferPool(int bufferSize, int poolSize) {
        this.bufferSize = bufferSize;
        this.pool = new ZeroAllocPool<>(PoolConfig.of(poolSize), () -> new PooledByteBuffer(bufferSize));
    }

    public PooledByteBuffer acquire() {
        PooledByteBuffer buf = pool.acquire();
        buf.markInUse();
        return buf;
    }

    public void release(PooledByteBuffer buf) {
        pool.release(buf);
    }

    public int bufferSize() {
        return bufferSize;
    }
}
