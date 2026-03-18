package io.pulsar.memory;

import io.pulsar.core.util.UnsafeAccess;
import java.util.ArrayList;
import java.util.List;

/**
 * A slab allocator that manages fixed-size memory blocks (slabs).
 * Provides O(1) allocation and deallocation for same-size objects.
 */
public final class SlabAllocator implements AutoCloseable {

    private final int slabSize;
    private final List<long[]> slabs = new ArrayList<>();
    private long[] freeList;
    private int freeTop = -1;

    public SlabAllocator(int slabSize, int initialSlabs) {
        this.slabSize = slabSize;
        this.freeList = new long[initialSlabs * 2];
        for (int i = 0; i < initialSlabs; i++) {
            long addr = UnsafeAccess.allocateMemory(slabSize);
            slabs.add(new long[]{addr});
            push(addr);
        }
    }

    public long allocate() {
        if (freeTop < 0) {
            long addr = UnsafeAccess.allocateMemory(slabSize);
            slabs.add(new long[]{addr});
            return addr;
        }
        return pop();
    }

    public void free(long address) { push(address); }

    public int slabSize() { return slabSize; }

    private void push(long addr) {
        if (freeTop + 1 >= freeList.length) {
            long[] grown = new long[freeList.length * 2];
            System.arraycopy(freeList, 0, grown, 0, freeList.length);
            freeList = grown;
        }
        freeList[++freeTop] = addr;
    }

    private long pop() { return freeList[freeTop--]; }

    @Override
    public void close() {
        for (long[] slab : slabs) UnsafeAccess.freeMemory(slab[0]);
        slabs.clear();
        freeTop = -1;
    }
}
