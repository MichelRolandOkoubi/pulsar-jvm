package io.pulsar.spring.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

/**
 * Contributes Pulsar pool metrics to Spring Boot Actuator's /info endpoint.
 */
public class PoolMetricsContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("pulsar-pool", java.util.Map.of(
            "status", "active"
        ));
    }
}
