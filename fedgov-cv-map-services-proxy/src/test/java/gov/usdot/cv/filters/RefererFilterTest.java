/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.usdot.cv.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

class RefererFilterTest {
    private RefererFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RefererFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    void testDoFilter_AllowsValidReferer() throws Exception {
        // Simulate allowed referer
        when(request.getHeader("Referer")).thenReturn("http://allowed.com");
        // Set allowedReferer via reflection (since @Value won't inject in unit test)
        java.lang.reflect.Field field = RefererFilter.class.getDeclaredField("allowedReferer");
        field.setAccessible(true);
        field.set(filter, "allowed.com");

        filter.doFilter(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testDoFilter_BlocksMissingReferer() throws Exception {
        when(request.getHeader("Referer")).thenReturn(null);
        filter.doFilter(request, response, chain);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Referer header required");
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void testDoFilter_BlocksInvalidReferer() throws Exception {
        when(request.getHeader("Referer")).thenReturn("http://notallowed.com");
        java.lang.reflect.Field field = RefererFilter.class.getDeclaredField("allowedReferer");
        field.setAccessible(true);
        field.set(filter, "allowed.com");

        filter.doFilter(request, response, chain);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Referer header");
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void testDoFilter_AllowsWhenAllowedRefererIsEmpty() throws Exception {
        when(request.getHeader("Referer")).thenReturn("http://anything.com");
        java.lang.reflect.Field field = RefererFilter.class.getDeclaredField("allowedReferer");
        field.setAccessible(true);
        field.set(filter, "");

        filter.doFilter(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }
}
