package io.pulsar.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Pulsar JVM Spring Boot integration.
 * All properties are prefixed with {@code pulsar}.
 */
@ConfigurationProperties(prefix = "pulsar")
public class PulsarProperties {

    private Pool pool = new Pool();

    public Pool getPool() { return pool; }
    public void setPool(Pool pool) { this.pool = pool; }

    public static class Pool {
        private int maxSize = 256;
        private boolean preallocate = true;

        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
        public boolean isPreallocate() { return preallocate; }
        public void setPreallocate(boolean preallocate) { this.preallocate = preallocate; }
    }
}
