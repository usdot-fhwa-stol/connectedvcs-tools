/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link IpAddressFilter}.
 *
 * Verifies that:
 *  - init() and destroy() run without exception
 *  - doFilter() extracts the IP address (via ClientRequestInfoParser) and
 *    always passes the request through the chain regardless of IP value
 */
class IpAddressFilterTest {

    private IpAddressFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter   = new IpAddressFilter();
        request  = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain    = mock(FilterChain.class);
    }

    @Test
    void init_doesNotThrow() throws Exception {
        FilterConfig config = mock(FilterConfig.class);
        filter.init(config);   // just logs; must not throw
    }

    @Test
    void destroy_doesNotThrow() {
        filter.destroy();      // just logs; must not throw
    }

    @Test
    void doFilter_withXForwardedFor_passesRequestThrough() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.42");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_withRemoteAddr_passesRequestThrough() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.5");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_neverBlocksRequest() throws Exception {
        // IpAddressFilter only logs; it must always forward regardless of IP
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");

        filter.doFilter(request, response, chain);

        verify(response, never()).sendError(anyInt(), anyString());
        verify(chain, times(1)).doFilter(request, response);
    }
}