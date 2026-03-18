package io.pulsar.spring.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * A pooled wrapper around {@link HttpServletRequest} that enables request object reuse.
 */
public class PooledRequestWrapper extends HttpServletRequestWrapper {

    public PooledRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void reset(HttpServletRequest request) {
        // Re-initialize the wrapper with a new underlying request
    }
}
