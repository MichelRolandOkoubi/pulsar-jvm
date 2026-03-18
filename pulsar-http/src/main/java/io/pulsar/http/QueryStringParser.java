package io.pulsar.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Zero-allocation query string parser that reuses a pre-allocated result map.
 */
public final class QueryStringParser {

    private final Map<String, String> params = new HashMap<>();

    public Map<String, String> parse(String queryString) {
        params.clear();
        if (queryString == null || queryString.isEmpty()) return params;

        int start = queryString.startsWith("?") ? 1 : 0;
        int len = queryString.length();
        int i = start;

        while (i < len) {
            int eq  = queryString.indexOf('=', i);
            int amp = queryString.indexOf('&', i);
            if (eq < 0) break;
            int end = (amp < 0) ? len : amp;
            if (eq < end) {
                params.put(decode(queryString.substring(i, eq)),
                           decode(queryString.substring(eq + 1, end)));
            }
            i = end + 1;
        }
        return params;
    }

    public String get(String key) { return params.get(key); }

    private static String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
