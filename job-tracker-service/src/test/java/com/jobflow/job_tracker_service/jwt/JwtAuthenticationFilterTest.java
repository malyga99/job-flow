package com.jobflow.job_tracker_service.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationFilter authenticationFilter;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilterInternal_withoutAuthHeader_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_notJwtTokenInHeader_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Notbearer " + "token");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_userIdIsNull_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + "token");
        when(jwtService.extractUserId("token")).thenReturn(null);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_userAlreadyAuthenticated_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + "token");
        when(jwtService.extractUserId("token")).thenReturn("1");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_tokenIsValid_setAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + "token");
        when(jwtService.extractUserId("token")).thenReturn("1");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(), "1");
        verify(filterChain, times(1)).doFilter(request, response);
    }
}