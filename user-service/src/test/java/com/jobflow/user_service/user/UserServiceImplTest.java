package com.jobflow.user_service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {


    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
        user = new User(1L, "Ivan", "Ivanov", "IvanIvanov@gmail.com", "abcde", Role.ROLE_USER);
    }

    @Test
    public void getCurrentUser_returnCurrentUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        User result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals(user, result);

        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();
    }

    @Test
    public void getCurrentUser_withoutAuthentication_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(null);

        AuthenticationCredentialsNotFoundException authenticationException = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userService.getCurrentUser());
        assertEquals("Current user is not authenticated", authenticationException.getMessage());
    }

    @Test
    public void getCurrentUser_notAuthenticated_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        AuthenticationCredentialsNotFoundException authenticationException = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userService.getCurrentUser());
        assertEquals("Current user is not authenticated", authenticationException.getMessage());

        verify(authentication, times(1)).isAuthenticated();
    }

}