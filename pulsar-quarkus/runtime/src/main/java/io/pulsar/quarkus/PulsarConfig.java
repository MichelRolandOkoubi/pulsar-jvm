package io.pulsar.quarkus;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Quarkus configuration for the Pulsar JVM extension.
 */
@ConfigMapping(prefix = "pulsar")
public interface PulsarConfig {

    Pool pool();

    interface Pool {
        @WithDefault("256")
        int maxSize();

        @WithDefault("true")
        boolean preallocate();
    }
}
