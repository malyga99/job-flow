package com.jobflow.user_service.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class IpUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Test
    public void extractClientIp_returnClientIp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("first-ip, second-ip, third-ip");

        String result = IpUtils.extractClientIp(request);

        assertNotNull(result);
        assertEquals("first-ip", result);
    }

    @Test
    public void extractClientIp_ipIsNull_returnRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("remote-addr");

        String result = IpUtils.extractClientIp(request);

        assertNotNull(result);
        assertEquals("remote-addr", result);
    }

    @Test
    public void extractClientIp_ipIsEmpty_returnRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("remote-addr");

        String result = IpUtils.extractClientIp(request);

        assertNotNull(result);
        assertEquals("remote-addr", result);
    }

}