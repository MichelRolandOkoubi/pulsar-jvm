package io.pulsar.benchmark;

import io.pulsar.http.ZeroCopyHttpParser;
import org.openjdk.jmh.annotations.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class HttpBenchmark {

    private static final String RAW_REQUEST =
        "GET /api/v1/users?page=1 HTTP/1.1\r\n" +
        "Host: localhost:8080\r\n" +
        "Accept: application/json\r\n" +
        "Connection: keep-alive\r\n" +
        "\r\n";

    private final byte[] requestBytes = RAW_REQUEST.getBytes(StandardCharsets.US_ASCII);

    @Benchmark
    public Object parseHttpRequest() {
        return ZeroCopyHttpParser.parseRequest(requestBytes);
    }
}
