package io.pulsar.benchmark;

import io.pulsar.core.pool.ObjectPool;
import io.pulsar.core.pool.PoolConfig;
import io.pulsar.core.pool.PooledObject;
import io.pulsar.core.pool.ZeroAllocPool;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class PoolBenchmark {

    static class BenchObj implements PooledObject {
        int value;
        @Override public void reset() { value = 0; }
    }

    private ZeroAllocPool<BenchObj> pool;

    @Setup
    public void setup() {
        pool = new ZeroAllocPool<>(PoolConfig.of(1024), BenchObj::new);
    }

    @Benchmark
    public BenchObj acquireAndRelease() {
        BenchObj obj = pool.acquire();
        pool.release(obj);
        return obj;
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder().include(PoolBenchmark.class.getSimpleName()).build()).run();
    }
}
