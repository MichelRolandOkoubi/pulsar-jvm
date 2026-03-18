package io.pulsar.http;

/**
 * Represents an HTTP response to be serialized.
 */
public final class HttpResponse {

    private final int statusCode;
    private final String reasonPhrase;
    private final HeadersMap headers;
    private final byte[] body;

    public HttpResponse(int statusCode, String reasonPhrase, HeadersMap headers, byte[] body) {
        this.statusCode   = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers      = headers;
        this.body         = body;
    }

    public static HttpResponse ok(byte[] body) {
        HeadersMap h = new HeadersMap(4);
        h.add("content-length", String.valueOf(body.length));
        return new HttpResponse(200, "OK", h, body);
    }

    public int statusCode()        { return statusCode;   }
    public String reasonPhrase()   { return reasonPhrase; }
    public HeadersMap headers()    { return headers;      }
    public byte[] body()           { return body;         }

    public byte[] toBytes() {
        String startLine = "HTTP/1.1 " + statusCode + " " + reasonPhrase + "\r\n";
        String headerStr = headers.toHeaderString() + "\r\n";
        byte[] sl = startLine.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] hs = headerStr.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] out = new byte[sl.length + hs.length + body.length];
        System.arraycopy(sl, 0, out, 0, sl.length);
        System.arraycopy(hs, 0, out, sl.length, hs.length);
        System.arraycopy(body, 0, out, sl.length + hs.length, body.length);
        return out;
    }
}
