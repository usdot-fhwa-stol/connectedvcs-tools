/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ClientRequestInfoParser}.
 *
 * Two branches:
 *   1. X-Forwarded-For header present → return that value
 *   2. X-Forwarded-For header absent (null) → fall back to getRemoteAddr()
 */
@ExtendWith(MockitoExtension.class)
class ClientRequestInfoParserTest {

    @Test
    void getClientIPAddress_xForwardedForPresent_returnsThatValue() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn("203.0.113.42");

        String ip = new ClientRequestInfoParser(req).getClientIPAddress();

        assertEquals("203.0.113.42", ip);
        verify(req, never()).getRemoteAddr();
    }

    @Test
    void getClientIPAddress_xForwardedForAbsent_fallsBackToRemoteAddr() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn(null);
        when(req.getRemoteAddr()).thenReturn("192.168.1.100");

        String ip = new ClientRequestInfoParser(req).getClientIPAddress();

        assertEquals("192.168.1.100", ip);
        verify(req).getRemoteAddr();
    }

    @Test
    void getClientIPAddress_xForwardedForContainsMultipleIps_returnsFullHeader() {
        // Proxy chains put multiple IPs in X-Forwarded-For; the class returns the raw value
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 10.0.0.1");

        String ip = new ClientRequestInfoParser(req).getClientIPAddress();

        assertEquals("203.0.113.1, 10.0.0.1", ip);
    }
}