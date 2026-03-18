package io.pulsar.spring.aop;

import java.lang.annotation.*;

/**
 * Marks a method or class as pool-managed. When applied, Pulsar JVM will ensure
 * that method parameters implementing {@link io.pulsar.core.pool.PooledObject} are
 * acquired from and returned to the appropriate pool.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pooled {
    /** Optional pool name for named pool lookups. */
    String value() default "";
}
