package io.pulsar.quarkus;

import io.pulsar.core.pool.PoolConfig;
import io.pulsar.core.pool.ZeroAllocPool;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * CDI producer for Pulsar JVM beans in a Quarkus application context.
 */
@Singleton
public class PulsarProducer {

    private final PulsarConfig config;

    public PulsarProducer(PulsarConfig config) {
        this.config = config;
    }

    @Produces
    @Singleton
    public PoolConfig poolConfig() {
        return new PoolConfig(config.pool().maxSize(), config.pool().preallocate(), false);
    }
}
