package io.pulsar.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import io.pulsar.core.pool.PoolConfig;
import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PulsarAutoConfiguration.class));

    @Test
    void poolConfigBeanIsCreated() {
        runner.run(ctx -> assertThat(ctx).hasSingleBean(PoolConfig.class));
    }

    @Test
    void customPoolSizeIsApplied() {
        runner.withPropertyValues("pulsar.pool.max-size=512")
              .run(ctx -> {
                  PoolConfig config = ctx.getBean(PoolConfig.class);
                  assertThat(config.maxPoolSize()).isEqualTo(512);
              });
    }
}
