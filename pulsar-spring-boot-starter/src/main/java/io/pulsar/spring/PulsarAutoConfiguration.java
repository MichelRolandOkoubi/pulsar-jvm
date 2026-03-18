package io.pulsar.spring;

import io.pulsar.core.pool.PoolConfig;
import io.pulsar.core.pool.ZeroAllocPool;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for Pulsar JVM components.
 */
@AutoConfiguration
@EnableConfigurationProperties(PulsarProperties.class)
public class PulsarAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PoolConfig pulsarPoolConfig(PulsarProperties props) {
        return new PoolConfig(props.getPool().getMaxSize(), props.getPool().isPreallocate(), false);
    }
}
