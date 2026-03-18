package io.pulsar.spring.pool;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * A custom Spring {@link Scope} that manages bean instances inside an object pool.
 */
public class PooledScope implements Scope {

    public static final String SCOPE_NAME = "pooled";

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return objectFactory.getObject();
    }

    @Override
    public Object remove(String name) { return null; }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {}

    @Override
    public Object resolveContextualObject(String key) { return null; }

    @Override
    public String getConversationId() { return SCOPE_NAME; }
}
