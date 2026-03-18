package io.pulsar.http;

import io.pulsar.core.collections.IntObjectMap;

/**
 * A compact, case-insensitive HTTP headers map backed by a primitive map.
 */
public final class HeadersMap {

    // Simple array-based storage: name-value pairs
    private final String[] names;
    private final String[] values;
    private int size;

    public HeadersMap(int initialCapacity) {
        this.names  = new String[initialCapacity];
        this.values = new String[initialCapacity];
    }

    public void add(String name, String value) {
        if (size >= names.length) throw new IllegalStateException("HeadersMap capacity exceeded");
        names[size]  = name;
        values[size] = value;
        size++;
    }

    public String get(String name) {
        for (int i = 0; i < size; i++) {
            if (names[i].equalsIgnoreCase(name)) return values[i];
        }
        return null;
    }

    public boolean contains(String name) { return get(name) != null; }

    public int size() { return size; }

    public String toHeaderString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) sb.append(names[i]).append(": ").append(values[i]).append("\r\n");
        return sb.toString();
    }
}
