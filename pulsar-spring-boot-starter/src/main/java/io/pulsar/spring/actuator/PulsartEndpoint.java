package io.pulsar.spring.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import java.util.Map;

/**
 * Spring Boot Actuator endpoint exposing Pulsar JVM pool statistics.
 */
@Endpoint(id = "pulsar")
public class PulsartEndpoint {

    @ReadOperation
    public Map<String, Object> info() {
        return Map.of(
            "status", "running",
            "version", "1.0.0-SNAPSHOT"
        );
    }
}
