package io.pulsar.core.strings;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A string interning cache to avoid duplicate {@code String} instances in
 * memory.
 * Unlike {@link String#intern()}, this implementation uses a bounded
 * {@code ConcurrentHashMap}
 * to avoid PermGen/Metaspace pressure.
 */
public final class StringInterner {

    private final ConcurrentHashMap<String, String> cache;
    private final int maxSize;

    public StringInterner(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>(maxSize);
    }

    /**
     * Returns a canonical (interned) version of the given string.
     */
    public String intern(String s) {
        if (s == null)
            return null;
        String existing = cache.get(s);
        if (existing != null)
            return existing;
        if (cache.size() < maxSize) {
            cache.putIfAbsent(s, s);
            return cache.get(s);
        }
        return s; // fallback: return as-is when cache is full
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }
}
