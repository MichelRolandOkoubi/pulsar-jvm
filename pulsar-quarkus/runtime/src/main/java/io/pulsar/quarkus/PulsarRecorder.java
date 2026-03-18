package io.pulsar.quarkus;

import io.quarkus.runtime.annotations.Recorder;

/**
 * Quarkus recorder for Pulsar JVM bootstrap at runtime.
 */
@Recorder
public class PulsarRecorder {

    public void initializePulsar(PulsarConfig config) {
        // Initialize pool configuration at runtime
    }
}
