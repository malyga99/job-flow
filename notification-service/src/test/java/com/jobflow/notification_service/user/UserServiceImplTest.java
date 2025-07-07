package com.jobflow.notification_service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void getCurrentUserId_returnCurrentUserId() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("1");

        Long result = userService.getCurrentUserId();

        assertNotNull(result);
        assertEquals(1L, result);

        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();
    }

    @Test
    public void getCurrentUserId_withoutAuthentication_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(null);

        var authenticationException = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userService.getCurrentUserId());
        assertEquals("Current user is not authenticated", authenticationException.getMessage());
    }

    @Test
    public void getCurrentUser_notAuthenticated_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        var authenticationException = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userService.getCurrentUserId());
        assertEquals("Current user is not authenticated", authenticationException.getMessage());

        verify(authentication, times(1)).isAuthenticated();
    }
}