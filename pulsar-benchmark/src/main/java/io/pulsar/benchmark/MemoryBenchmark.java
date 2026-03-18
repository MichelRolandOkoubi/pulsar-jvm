package io.pulsar.benchmark;

import io.pulsar.memory.ArenaAllocator;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class MemoryBenchmark {

    private ArenaAllocator arena;

    @Setup
    public void setup() {
        arena = new ArenaAllocator(1024 * 1024); // 1 MB arena
    }

    @TearDown
    public void tearDown() {
        arena.close();
    }

    @Benchmark
    public long arenaBumpAlloc() {
        long addr = arena.allocate(64);
        arena.reset();
        return addr;
    }

    @Benchmark
    public byte[] jvmHeapAlloc() {
        return new byte[64];
    }
}
