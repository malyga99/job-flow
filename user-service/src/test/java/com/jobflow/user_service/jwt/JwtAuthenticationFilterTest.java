package com.jobflow.user_service.jwt;

import com.jobflow.user_service.TestUtil;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

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

    @Mock
    private UserDetailsService userDetailsService;

    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter authenticationFilter;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();

        userDetails = TestUtil.createUser();
    }

    @Test
    public void doFilterInternal_withoutAuthHeader_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_notJwtTokenInHeader_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Notbearer " + TestUtil.ACCESS_TOKEN);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_userIdIsNull_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TestUtil.ACCESS_TOKEN);
        when(jwtService.extractUserId(TestUtil.ACCESS_TOKEN)).thenReturn(null);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_userAlreadyAuthenticated_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TestUtil.ACCESS_TOKEN);
        when(jwtService.extractUserId(TestUtil.ACCESS_TOKEN)).thenReturn(TestUtil.USER_ID);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_tokenNotValid_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TestUtil.ACCESS_TOKEN);
        when(jwtService.extractUserId(TestUtil.ACCESS_TOKEN)).thenReturn(TestUtil.USER_ID);
        when(userDetailsService.loadUserByUsername(TestUtil.USER_ID)).thenReturn(userDetails);
        when(jwtService.isValid(userDetails, TestUtil.ACCESS_TOKEN)).thenReturn(false);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_tokenIsValid_setAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TestUtil.ACCESS_TOKEN);
        when(jwtService.extractUserId(TestUtil.ACCESS_TOKEN)).thenReturn(TestUtil.USER_ID);
        when(userDetailsService.loadUserByUsername(TestUtil.USER_ID)).thenReturn(userDetails);
        when(jwtService.isValid(userDetails, TestUtil.ACCESS_TOKEN)).thenReturn(true);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(), TestUtil.USER_ID);
        verify(filterChain, times(1)).doFilter(request, response);
    }

}