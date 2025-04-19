package com.jobflow.user_service.jwt;

import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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

    private static final String JWT_TOKEN = "my.jwt.token";
    private static final String LOGIN = "IvanIvanov@gmail.com";

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
        userDetails = new User(1L, "Ivan", "Ivanov", "IvanIvanov@gmail.com", "abcde", Role.ROLE_USER);
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
        when(request.getHeader("Authorization")).thenReturn("Notbearer " + JWT_TOKEN);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_loginIsNull_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
        when(jwtService.extractLogin(JWT_TOKEN)).thenReturn(null);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_userAlreadyAuthenticated_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
        when(jwtService.extractLogin(JWT_TOKEN)).thenReturn(LOGIN);
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
        when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
        when(jwtService.extractLogin(JWT_TOKEN)).thenReturn(LOGIN);
        when(userDetailsService.loadUserByUsername(LOGIN)).thenReturn(userDetails);
        when(jwtService.isValid(userDetails, JWT_TOKEN)).thenReturn(false);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_tokenIsValid_setAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
        when(jwtService.extractLogin(JWT_TOKEN)).thenReturn(LOGIN);
        when(userDetailsService.loadUserByUsername(LOGIN)).thenReturn(userDetails);
        when(jwtService.isValid(userDetails, JWT_TOKEN)).thenReturn(true);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(), LOGIN);
        verify(filterChain, times(1)).doFilter(request, response);
    }

}