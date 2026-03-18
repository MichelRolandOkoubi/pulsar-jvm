package io.pulsar.http;

/**
 * Represents a parsed HTTP request.
 */
public final class HttpRequest {

    private final String method;
    private final String uri;
    private final HeadersMap headers;
    private final byte[] body;

    public HttpRequest(String method, String uri, HeadersMap headers, byte[] body) {
        this.method  = method;
        this.uri     = uri;
        this.headers = headers;
        this.body    = body;
    }

    public String method()      { return method;  }
    public String uri()         { return uri;     }
    public HeadersMap headers() { return headers; }
    public byte[] body()        { return body;    }

    public String header(String name) { return headers.get(name.toLowerCase()); }

    @Override
    public String toString() {
        return method + " " + uri;
    }
}
