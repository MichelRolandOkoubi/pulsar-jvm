package io.pulsar.benchmark;

import io.ultralight.json.ZeroCopyJsonParser;
import io.ultralight.json.ZeroCopyJsonWriter;
import org.openjdk.jmh.annotations.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class JsonBenchmark {

    private static final String SAMPLE = "{\"id\":42,\"name\":\"pulsar\",\"active\":true}";
    private final byte[] sampleBytes = SAMPLE.getBytes(StandardCharsets.UTF_8);
    private final ZeroCopyJsonWriter writer = new ZeroCopyJsonWriter(256);

    @Benchmark
    public Object parseJson() {
        return new ZeroCopyJsonParser(sampleBytes).nextToken();
    }

    @Benchmark
    public byte[] writeJson() {
        writer.reset();
        writer.startObject()
              .field("id").number(42L).comma()
              .field("name").string("pulsar").comma()
              .field("active").bool(true)
              .endObject();
        return writer.toBytes();
    }
}
