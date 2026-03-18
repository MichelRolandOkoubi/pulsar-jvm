package io.pulsar.core.strings;

/**
 * A zero-copy string view over a byte array region, avoiding heap allocation.
 * Useful when parsing network data where you need to reference substrings
 * without copying.
 */
public final class ZeroCopyString implements CharSequence {

    private final byte[] data;
    private final int offset;
    private final int length;

    public ZeroCopyString(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return (char) (data[offset + index] & 0xFF);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new ZeroCopyString(data, offset + start, end - start);
    }

    public String toStringCopy() {
        return Utf8Utils.fromBytes(data, offset, length);
    }

    @Override
    public String toString() {
        return toStringCopy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ZeroCopyString z) {
            if (z.length != length)
                return false;
            for (int i = 0; i < length; i++) {
                if (data[offset + i] != z.data[z.offset + i])
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (int i = 0; i < length; i++)
            h = 31 * h + data[offset + i];
        return h;
    }
}
