package io.pulsar.core.strings;

import java.nio.charset.StandardCharsets;

/**
 * Utility methods for working with UTF-8 bytes without allocating intermediate
 * Strings.
 */
public final class Utf8Utils {

    private Utf8Utils() {
    }

    public static byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String fromBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String fromBytes(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }

    /**
     * Computes the UTF-8 encoded length of a string without actually encoding it.
     */
    public static int encodedLength(String s) {
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x80)
                len += 1;
            else if (c < 0x800)
                len += 2;
            else if (Character.isSurrogate(c))
                len += 4;
            else
                len += 3;
        }
        return len;
    }
}
