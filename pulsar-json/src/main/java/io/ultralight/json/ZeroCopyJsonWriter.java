package io.ultralight.json;

import java.nio.charset.StandardCharsets;

/**
 * A zero-allocation JSON writer that builds JSON into a pre-allocated byte array.
 */
public final class ZeroCopyJsonWriter {

    private final byte[] buf;
    private int pos;

    public ZeroCopyJsonWriter(int capacity) {
        this.buf = new byte[capacity];
    }

    public ZeroCopyJsonWriter startObject()  { write('{'); return this; }
    public ZeroCopyJsonWriter endObject()    { write('}'); return this; }
    public ZeroCopyJsonWriter startArray()   { write('['); return this; }
    public ZeroCopyJsonWriter endArray()     { write(']'); return this; }
    public ZeroCopyJsonWriter comma()        { write(','); return this; }
    public ZeroCopyJsonWriter colon()        { write(':'); return this; }

    public ZeroCopyJsonWriter string(String value) {
        write('"');
        writeBytes(value.getBytes(StandardCharsets.UTF_8));
        write('"');
        return this;
    }

    public ZeroCopyJsonWriter field(String name) {
        return string(name).colon();
    }

    public ZeroCopyJsonWriter number(long value) {
        writeBytes(Long.toString(value).getBytes(StandardCharsets.US_ASCII));
        return this;
    }

    public ZeroCopyJsonWriter number(double value) {
        writeBytes(Double.toString(value).getBytes(StandardCharsets.US_ASCII));
        return this;
    }

    public ZeroCopyJsonWriter bool(boolean value) {
        writeBytes((value ? "true" : "false").getBytes(StandardCharsets.US_ASCII));
        return this;
    }

    public ZeroCopyJsonWriter nullValue() {
        writeBytes("null".getBytes(StandardCharsets.US_ASCII));
        return this;
    }

    public byte[] toBytes() {
        byte[] result = new byte[pos];
        System.arraycopy(buf, 0, result, 0, pos);
        return result;
    }

    public void reset() { pos = 0; }

    private void write(char c) { buf[pos++] = (byte) c; }

    private void writeBytes(byte[] bytes) {
        System.arraycopy(bytes, 0, buf, pos, bytes.length);
        pos += bytes.length;
    }
}
