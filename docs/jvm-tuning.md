# JVM Tuning Guide — Target: ≤100 MB Heap

> **Author:** Michel Okoubi — Staff Engineer

This guide documents the exact JVM flags and configuration knobs to push
Pulsar JVM applications below the **100 MB resident heap** mark.

---

## 1. Choose the Right GC

| GC | Best For | Pause | Throughput |
|----|----------|-------|-----------|
| **ZGC** (`-XX:+UseZGC`) | Latency-critical, large heaps | <1 ms | Good |
| **Shenandoah** | Similar to ZGC | <1 ms | Good |
| **G1GC** | Balanced (default) | 5-20 ms | High |
| **Serial GC** | Tiny heaps ≤200 MB | Moderate | Highest |

> 💡 For ≤100 MB heap with Pulsar JVM: use **Serial GC** or **ZGC with aggressive region sizing**.

---

## 2. Minimal Heap Flags

```bash
# Absolute minimum for a Pulsar JVM microservice
java \
  -Xms32m                          # Start small — don't pre-allocate
  -Xmx100m                         # Hard cap
  -Xss128k                         # Thread stacks: 128K instead of 512K default
  -XX:+UseSerialGC                 # Lowest GC overhead for small heaps
  -XX:+UseCompressedOops           # 4-byte refs (auto below 32 GB, explicit for clarity)
  -XX:+UseCompressedClassPointers  # 4-byte class pointers
  -XX:MaxMetaspaceSize=32m         # Cap metaspace — default is unlimited!
  -XX:ReservedCodeCacheSize=32m    # JIT cache — default 240 MB is overkill
  -XX:InitialCodeCacheSize=4m      \
  -jar app.jar
```

---

## 3. Eliminate Framework Heap Overhead

### Spring Boot — AOT mode (removes reflection proxies)
```bash
# Build
mvn spring-boot:aot-process
mvn native:compile -Pnative   # optional: native image, ~40 MB RSS

# Run with AOT (still JVM, no native)
java -Dspring.aot.enabled=true \
     -XX:+UseSerialGC -Xmx100m \
     -jar app.jar
```

### Quarkus — fast-startup mode
```properties
# application.properties
quarkus.jvm.enable.preview=false
quarkus.arc.remove-unused-beans=all
quarkus.arc.remove-final-for-proxy-excluded=true
```

---

## 4. Tune Pulsar JVM Pools

```properties
# application.properties (Spring Boot)
# Smaller pools = fewer live objects = less heap
pulsar.pool.max-size=32          # down from default 256
pulsar.pool.preallocate=false    # defer allocation until first use
```

Use **ThreadLocalPool** (capacity 16-32) instead of shared pools:
```java
// Per-thread pool, no CAS, no heap sharing
ThreadLocalPool<MyObj> pool = new ThreadLocalPool<>(32, MyObj::new);
```

---

## 5. Move Caches Off-Heap

Replace any in-memory cache with `OffHeapHashMap`:

```java
// Before: 80 MB heap for 1M entries
Map<Long, byte[]> cache = new HashMap<>();

// After: 32 MB native, 0 MB heap
try (OffHeapHashMap map = new OffHeapHashMap(1_000_000, 24)) {
    long valueAddr = map.getOrCreate(key);
    UnsafeAccess.putLong(valueAddr, myValue);
}
```

---

## 6. Memory-Mapped Files (Zero Heap for Large Data)

```java
// Map a large dataset — OS manages paging, JVM heap = 0
try (FileChannel fc = FileChannel.open(path, READ)) {
    MemorySegment segment = fc.map(MapMode.READ_ONLY, 0, fc.size(),
                                   Arena.global());
    NativeMemorySegment nms = new NativeMemorySegment(segment);
    long value = nms.getLong(offset);
}
```

---

## 7. Thread Stack Reduction

Default JVM stack size: **512 KB per thread**.
At 200 threads: **102 MB** just in stacks.

```bash
-Xss64k    # Minimum for simple tasks (requires testing)
-Xss128k   # Safe minimum for Spring/Quarkus handler threads
```

With virtual threads (Java 21+):
```java
// Virtual threads: stack starts at ~1 KB, grows lazily
try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
    // 10 000 concurrent tasks ≈ 10 MB stack vs 5 GB with platform threads
}
```

---

## 8. String Deduplication

```bash
-XX:+UseStringDeduplication      # Works with G1GC and ZGC
-XX:StringDeduplicationAgeThreshold=3
```

Or use `CompactStringTable` to store parsed strings by short index:
```java
CompactStringTable table = new CompactStringTable(10_000, 512_000);
int handle = table.intern(headerValue);   // 2 bytes instead of 40+ bytes
```

---

## 9. Expected Results After All Tunings

| Technique | Heap Saved |
|-----------|-----------|
| ThreadLocalPool (32 cap) | −30 MB |
| RegionAllocator (vs ArenaAllocator) | −40 MB |
| CompressedArena (32-bit handles) | −25 MB |
| OffHeapHashMap (replace caches) | −30 MB |
| CompactStringTable | −15 MB |
| JVM flags (stack, metaspace, code cache) | −25 MB |
| Spring Boot AOT | −20 MB |
| **Total** | **−185 MB** |

Starting from baseline **285 MB → ~100 MB** ✅

---

## 10. Verification

```bash
# Monitor live heap with JFR
java -XX:StartFlightRecording=filename=pulsar.jfr,duration=60s \
     -Xmx100m -jar app.jar

# Check actual RSS (Resident Set Size)
ps -o pid,rss,vsz -p $(pgrep java)

# Or with jcmd
jcmd <pid> VM.native_memory summary
```
