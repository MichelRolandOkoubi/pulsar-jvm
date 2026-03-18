# Best Practices

> **Author:** Michel Okoubi — Staff Engineer


## Object Pools

- **Always call `reset()` via `release()`** — the pool does this automatically, but ensure your `PooledObject.reset()` clears all mutable state.
- **Size pools appropriately** — overly large pools waste memory; too small causes fallback allocations.
- **Avoid escaping pooled objects** — don't store them in long-lived fields or pass them across threads without proper synchronization.

## Native Buffers

- **Use try-with-resources** — `NativeBuffer` implements `AutoCloseable`; always close it to avoid native memory leaks.
- **Prefer `DirectBufferPool`** — reuse `PooledByteBuffer` instances for I/O-heavy paths.

## Collections

- **Use primitive collections** — prefer `IntIntMap` / `IntObjectMap` over `HashMap<Integer, V>` to eliminate boxing.
- **Set a realistic initial capacity** — avoids rehashing and wasted memory.

## Zero-Copy Strings

- **Use `ZeroCopyString`** for read-only string views during parsing (HTTP headers, JSON keys).
- **Call `toStringCopy()` only when needed** — materializing the String allocates heap memory.

## Lock-Free Queues

- **Use `SpscQueue`** when you have exactly one producer and one consumer — it is significantly faster than `MpmcQueue`.
- **Size queues to powers of two** — this is enforced but good to keep in mind for capacity planning.
