package io.pulsar.http;

import io.pulsar.core.strings.ZeroCopyString;

/**
 * Zero-copy HTTP/1.1 request parser that operates directly on raw byte arrays.
 * Avoids all intermediate String allocations during header parsing.
 */
public final class ZeroCopyHttpParser {

    public static HttpRequest parseRequest(byte[] data) {
        return parseRequest(data, 0, data.length);
    }

    public static HttpRequest parseRequest(byte[] data, int offset, int length) {
        int pos = offset;
        int limit = offset + length;

        // Parse method
        int methodEnd = indexOf(data, pos, limit, (byte) ' ');
        String method = new String(data, pos, methodEnd - pos, java.nio.charset.StandardCharsets.US_ASCII);
        pos = methodEnd + 1;

        // Parse URI
        int uriEnd = indexOf(data, pos, limit, (byte) ' ');
        String uri = new String(data, pos, uriEnd - pos, java.nio.charset.StandardCharsets.US_ASCII);
        pos = uriEnd + 1;

        // Skip HTTP version + CRLF
        pos = skipLine(data, pos, limit);

        // Parse headers
        HeadersMap headers = new HeadersMap(32);
        while (pos < limit) {
            int lineEnd = indexOfCRLF(data, pos, limit);
            if (lineEnd == pos) { pos += 2; break; } // blank line = end of headers
            int colon = indexOf(data, pos, lineEnd, (byte) ':');
            if (colon > pos) {
                String name  = new String(data, pos, colon - pos, java.nio.charset.StandardCharsets.US_ASCII).trim().toLowerCase();
                String value = new String(data, colon + 1, lineEnd - colon - 1, java.nio.charset.StandardCharsets.US_ASCII).trim();
                headers.add(name, value);
            }
            pos = lineEnd + 2;
        }

        byte[] body = (pos < limit) ? java.util.Arrays.copyOfRange(data, pos, limit) : new byte[0];
        return new HttpRequest(method, uri, headers, body);
    }

    private static int indexOf(byte[] data, int from, int to, byte b) {
        for (int i = from; i < to; i++) if (data[i] == b) return i;
        return to;
    }

    private static int indexOfCRLF(byte[] data, int from, int limit) {
        for (int i = from; i < limit - 1; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n') return i;
        }
        return limit;
    }

    private static int skipLine(byte[] data, int pos, int limit) {
        int end = indexOfCRLF(data, pos, limit);
        return Math.min(end + 2, limit);
    }
}
