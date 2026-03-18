package io.pulsar.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * AOP aspect that intercepts methods annotated with {@link Pooled} and applies
 * pool lifecycle management around them.
 */
@Aspect
public class PooledAspect {

    @Around("@annotation(pooled)")
    public Object around(ProceedingJoinPoint pjp, Pooled pooled) throws Throwable {
        // Pre: acquire pooled resources
        try {
            return pjp.proceed();
        } finally {
            // Post: release pooled resources
        }
    }
}
