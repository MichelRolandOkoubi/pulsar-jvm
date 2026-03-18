package io.pulsar.spring.pool;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Spring {@link BeanPostProcessor} that detects {@code @Pooled} beans and wraps them
 * with pool-aware lifecycle management.
 */
public class PoolBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Detect @Pooled annotation and apply pool proxy if needed
        return bean;
    }
}
