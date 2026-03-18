package io.pulsar.benchmark;

import io.pulsar.core.collections.IntObjectMap;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class CollectionsBenchmark {

    private IntObjectMap<String> primitiveMap;
    private HashMap<Integer, String> jdkMap;

    @Setup
    public void setup() {
        primitiveMap = new IntObjectMap<>(1024);
        jdkMap = new HashMap<>(1024);
        for (int i = 0; i < 1000; i++) {
            primitiveMap.put(i, "value-" + i);
            jdkMap.put(i, "value-" + i);
        }
    }

    @Benchmark
    public String primitiveMapGet() {
        return primitiveMap.get(500);
    }

    @Benchmark
    public String jdkMapGet() {
        return jdkMap.get(500);
    }
}
