# Configuration Reference

> **Author:** Michel Okoubi — Staff Engineer


## pulsar-core

No external configuration required. Configure programmatically via `PoolConfig`:

```java
PoolConfig config = new PoolConfig(
    256,    // maxPoolSize
    true,   // preallocate
    false   // threadLocal
);
```

## Spring Boot

| Property | Default | Description |
|----------|---------|-------------|
| `pulsar.pool.max-size` | `256` | Maximum pool capacity |
| `pulsar.pool.preallocate` | `true` | Pre-create objects at startup |

## Quarkus

| Property | Default | Description |
|----------|---------|-------------|
| `pulsar.pool.max-size` | `256` | Maximum pool capacity |
| `pulsar.pool.preallocate` | `true` | Pre-create objects at startup |
