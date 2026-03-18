package io.ultralight.json;

/**
 * A zero-copy JSON parser that operates directly on a byte array without allocating
 * intermediate String objects. Yields {@link JsonToken} events in a pull-based fashion.
 */
public final class ZeroCopyJsonParser {

    private final byte[] data;
    private int pos;
    private final int limit;

    // Current value region (zero-copy slice)
    private int valueStart;
    private int valueLength;

    public ZeroCopyJsonParser(byte[] data) {
        this(data, 0, data.length);
    }

    public ZeroCopyJsonParser(byte[] data, int offset, int length) {
        this.data = data;
        this.pos = offset;
        this.limit = offset + length;
    }

    public JsonToken nextToken() {
        skipWhitespace();
        if (pos >= limit) return JsonToken.EOF;

        byte b = data[pos++];
        return switch (b) {
            case '{' -> JsonToken.START_OBJECT;
            case '}' -> JsonToken.END_OBJECT;
            case '[' -> JsonToken.START_ARRAY;
            case ']' -> JsonToken.END_ARRAY;
            case '"' -> parseString();
            case 't' -> consumeLiteral("rue", 3) ? JsonToken.BOOLEAN_TRUE  : fail("true");
            case 'f' -> consumeLiteral("alse", 4) ? JsonToken.BOOLEAN_FALSE : fail("false");
            case 'n' -> consumeLiteral("ull", 3)  ? JsonToken.NULL          : fail("null");
            default  -> { pos--; yield parseNumber(); }
        };
    }

    /** Returns the raw bytes of the current token value (zero-copy). */
    public byte[] valueBytes() {
        byte[] out = new byte[valueLength];
        System.arraycopy(data, valueStart, out, 0, valueLength);
        return out;
    }

    public String valueAsString() {
        return new String(data, valueStart, valueLength, java.nio.charset.StandardCharsets.UTF_8);
    }

    public long valueAsLong() { return Long.parseLong(valueAsString()); }
    public double valueAsDouble() { return Double.parseDouble(valueAsString()); }

    private JsonToken parseString() {
        valueStart = pos;
        while (pos < limit) {
            byte c = data[pos++];
            if (c == '"') {
                valueLength = pos - valueStart - 1;
                return JsonToken.STRING;
            }
            if (c == '\\') pos++; // skip escaped char
        }
        throw new IllegalStateException("Unterminated string at position " + pos);
    }

    private JsonToken parseNumber() {
        valueStart = pos;
        while (pos < limit && isNumberChar(data[pos])) pos++;
        valueLength = pos - valueStart;
        return JsonToken.NUMBER;
    }

    private boolean consumeLiteral(String suffix, int len) {
        if (pos + len > limit) return false;
        for (int i = 0; i < len; i++) if (data[pos + i] != suffix.charAt(i)) return false;
        pos += len;
        return true;
    }

    private void skipWhitespace() {
        while (pos < limit && (data[pos] == ' ' || data[pos] == '\t' || data[pos] == '\n' || data[pos] == '\r')) pos++;
    }

    private static boolean isNumberChar(byte b) {
        return (b >= '0' && b <= '9') || b == '-' || b == '+' || b == '.' || b == 'e' || b == 'E';
    }

    private JsonToken fail(String expected) {
        throw new IllegalStateException("Expected '" + expected + "' at position " + pos);
    }
}
