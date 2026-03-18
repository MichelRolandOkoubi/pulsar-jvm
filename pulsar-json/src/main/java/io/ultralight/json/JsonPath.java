package io.ultralight.json;

/**
 * Represents a JsonPath expression for navigating JSON structures.
 * Lightweight alternative to JSONPath for common use cases.
 */
public final class JsonPath {

    private final String[] segments;

    private JsonPath(String[] segments) {
        this.segments = segments;
    }

    public static JsonPath of(String path) {
        return new JsonPath(path.split("\\."));
    }

    public String[] segments() { return segments; }

    public int depth() { return segments.length; }

    @Override
    public String toString() { return String.join(".", segments); }
}
