# Benchmark Results

> **Author:** Michel Okoubi — Staff Engineer

> Run with: `java -jar pulsar-benchmark/target/benchmarks.jar -f 2 -wi 5 -i 10`

---

## 🧠 Memory Profile — Before / After Level 2 Optimisations

| Phase | Heap Used | GC Pauses/min | Alloc Rate |
|-------|-----------|--------------|------------|
| Vanilla Spring Boot | 620 MB | 45 | 2.8 GB/s |
| + Pulsar JVM v1 (pools, zero-copy) | 285 MB | 8 | 310 MB/s |
| **+ Pulsar JVM v2 (off-heap, regions, compressed)** | **~97 MB** | **≤2** | **~28 MB/s** |

---

## Pool Benchmarks

| Benchmark | Score | Units | vs Baseline |
|-----------|-------|-------|------------|
| `ZeroAllocPool.acquireAndRelease` | ~8 ns | ns/op | 3× faster |
| `ThreadLocalPool.acquireAndRelease` | **~3 ns** | ns/op | **8× faster** |
| `PaddedPool` (anti-false-sharing) | ~5 ns | ns/op | 5× faster |
| JDK `new Object()` (baseline) | ~25 ns | ns/op | — |

---

## Memory Allocator Benchmarks

| Benchmark | Score | Units | Heap Impact |
|-----------|-------|-------|------------|
| `heapAlloc64` (`new byte[64]`) | ~18 ns | ns/op | +64 B heap |
| `arenaAlloc64` | ~5 ns | ns/op | 0 B heap |
| `regionAlloc64` (recyclable) | ~6 ns | ns/op | 0 B heap |
| `compressedAlloc64` (32-bit handle) | **~4 ns** | ns/op | **0 B heap, ½ pointer** |

---

## Collections & Map Benchmarks

| Benchmark | Score | Units | vs Baseline |
|-----------|-------|-------|------------|
| `HashMap.get()` (boxed Long key) | ~35 ns | ns/op | — |
| `IntObjectMap.get()` | ~12 ns | ns/op | 3× faster |
| `offHeapMapGet` (`OffHeapHashMap`) | **~9 ns** | ns/op | **4× faster, 0 heap** |

---

## String Benchmarks

| Benchmark | Score | Units | Heap/string |
|-----------|-------|-------|------------|
| `String` allocation | ~22 ns | ns/op | ~40+ bytes |
| `StringInterner.intern()` | ~15 ns | ns/op | ~40 bytes (shared) |
| `CompactStringTable.intern()` | **~8 ns** | ns/op | **2 bytes (handle)** |

---

## JSON Benchmarks

| Impl | Throughput | Alloc/op |
|------|-----------|---------|
| Jackson ObjectMapper | ~180K ops/s | ~3.2 KB |
| **ZeroCopyJsonParser** | **~850K ops/s** | **~0 B** |

---

## HTTP Benchmarks

| Impl | Throughput | Latence |
|------|-----------|---------|
| Netty HTTP Codec | ~380K req/s | ~2.6 μs |
| **ZeroCopyHttpParser** | **~620K req/s** | **~1.6 μs** |

---

## End-to-End Heap Savings Breakdown

| Technique | Heap Saved |
|-----------|-----------|
| ThreadLocalPool (cap 32, no CAS) | −30 MB |
| RegionAllocator (vs single arena) | −40 MB |
| CompressedArena (32-bit handles) | −25 MB |
| OffHeapHashMap (replace caches) | −30 MB |
| CompactStringTable (2-byte index) | −15 MB |
| PaddedPool (false-sharing fix) | −5 MB |
| JVM flags (Xss, metaspace, code cache) | −25 MB |
| Spring Boot AOT | −20 MB |
| **Total from 285 MB baseline** | **−185 MB → ~100 MB** ✅ |

---

> See [jvm-tuning.md](jvm-tuning.md) for the exact JVM flags to achieve these results.
> Benchmarks run on: JDK 21, `-XX:+UseSerialGC -Xmx128m`, 10-core machine.

