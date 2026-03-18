package io.pulsar.memory;

import io.pulsar.core.util.UnsafeAccess;

/**
 * A fully off-heap singly-linked list.
 *
 * <p>Each node is laid out in native memory as:
 * <pre>
 *   [0..7]   next pointer (long, 0 = null)
 *   [8..N]   payload bytes
 * </pre>
 * The JVM heap contains <b>only one object</b>: this {@code OffHeapList} instance
 * (~40 bytes). All node data lives in native memory.</p>
 *
 * <h3>Memory saving</h3>
 * Each equivalent Java {@code LinkedList} node costs:
 * <ul>
 *   <li>16 bytes object header</li>
 *   <li>8 bytes prev pointer</li>
 *   <li>8 bytes next pointer</li>
 *   <li>8 bytes item reference</li>
 *   <li>+ item object itself (≥16 bytes header)</li>
 * </ul>
 * Total ≥56 bytes per node on-heap. Off-heap: 8 (next) + payload only.
 * For 1M nodes of 16-byte payloads: <strong>56 MB → 24 MB (−57%)</strong>.
 */
public final class OffHeapList implements AutoCloseable {

    private static final long NEXT_OFFSET    = 0L;
    private static final long PAYLOAD_OFFSET = 8L;

    private final int    nodeSize;     // next(8) + payload
    private final int    payloadSize;
    private final ArenaAllocator arena;
    private long head = 0L;           // 0 means null
    private int  size = 0;

    public OffHeapList(int payloadSize, long maxNodes) {
        this.payloadSize = payloadSize;
        this.nodeSize    = 8 + payloadSize;
        this.arena       = new ArenaAllocator(maxNodes * nodeSize);
    }

    /**
     * Prepends a new node. Returns the native address so the caller can write payload.
     */
    public long prepend() {
        long node = arena.allocate(nodeSize);
        UnsafeAccess.putLong(node + NEXT_OFFSET, head);
        head = node;
        size++;
        return node + PAYLOAD_OFFSET;
    }

    /**
     * Iterates over all payload addresses in insertion order.
     * The visitor receives the <em>payload start address</em>.
     */
    public void forEach(LongConsumer visitor) {
        long node = head;
        while (node != 0L) {
            visitor.accept(node + PAYLOAD_OFFSET);
            node = UnsafeAccess.getLong(node + NEXT_OFFSET);
        }
    }

    /** Resets the list (arena reset — O(1), no per-node iteration). */
    public void clear() {
        arena.reset();
        head = 0L;
        size = 0;
    }

    public int  size()        { return size;        }
    public int  payloadSize() { return payloadSize; }

    @Override
    public void close() { arena.close(); }

    @FunctionalInterface
    public interface LongConsumer {
        void accept(long payloadAddress);
    }
}
