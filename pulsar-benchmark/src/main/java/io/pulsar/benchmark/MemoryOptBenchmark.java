package io.pulsar.benchmark;

import io.pulsar.memory.*;
import io.pulsar.core.pool.*;
import io.pulsar.core.strings.CompactStringTable;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class MemoryOptBenchmark {

    // ---------------------------------------------------------------- setup --

    private ArenaAllocator        arena;
    private RegionAllocator       regionAllocator;
    private CompressedArena       compressedArena;
    private OffHeapHashMap        offHeapMap;
    private CompactStringTable    stringTable;
    private HashMap<Long, byte[]> heapMap;

    @Setup
    public void setup() {
        arena           = new ArenaAllocator(64 * 1024 * 1024L);
        regionAllocator = new RegionAllocator(256, 64 * 1024);
        compressedArena = new CompressedArena(64 * 1024 * 1024L);
        offHeapMap      = new OffHeapHashMap(16384, 24);
        stringTable     = new CompactStringTable(1000, 64 * 1024);
        heapMap         = new HashMap<>(16384);

        // Pre-populate maps
        for (int i = 0; i < 1000; i++) {
            offHeapMap.getOrCreate(i);
            heapMap.put((long) i, new byte[24]);
        }
    }

    @TearDown
    public void tearDown() {
        arena.close();
        regionAllocator.close();
        compressedArena.close();
        offHeapMap.close();
    }

    // ---------------------------------------------------------- benchmarks --

    /** Baseline: JVM heap allocation */
    @Benchmark
    public byte[] heapAlloc64() {
        return new byte[64];
    }

    /** Arena: O(1) bump pointer */
    @Benchmark
    public long arenaAlloc64() {
        long addr = arena.allocate(64);
        arena.reset();
        return addr;
    }

    /** Region: avoids fragmentation, recyclable independently */
    @Benchmark
    public long regionAlloc64() {
        return regionAllocator.allocate(64);
    }

    /** Compressed: 32-bit handle, half the pointer size */
    @Benchmark
    public int compressedAlloc64() {
        return compressedArena.allocate(64);
    }

    /** Off-heap map get: zero-GC lookup */
    @Benchmark
    public long offHeapMapGet() {
        return offHeapMap.get(500L);
    }

    /** Heap map get: GC-visible, boxed key */
    @Benchmark
    public Object heapMapGet() {
        return heapMap.get(500L);
    }

    /** CompactStringTable intern: 2-byte handle */
    @Benchmark
    public int compactStringIntern() {
        return stringTable.intern("Authorization");
    }
}
