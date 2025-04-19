package com.jobflow.user_service.auth;

import com.jobflow.user_service.exception.UserNotFoundException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private static final String ACCESS_TOKEN = "my.access.token";

    private static final String REFRESH_TOKEN = "my.refresh.token";

    private User user;

    private AuthenticationRequest authenticationRequest;

    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

    @BeforeEach
    public void setup() {
        authenticationRequest = new AuthenticationRequest("IvanIvanov@gmail.com", "abcde");
        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                authenticationRequest.getLogin().toLowerCase(),
                authenticationRequest.getPassword()
        );
        user = new User(1L, "Ivan", "Ivanov", authenticationRequest.getLogin(), authenticationRequest.getPassword(), Role.ROLE_USER);
    }

    @Test
    public void auth_returnAuthResponse() {
        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);

        AuthenticationResponse result = authenticationService.auth(authenticationRequest);

        assertNotNull(result);
        assertEquals(ACCESS_TOKEN, result.getAccessToken());
        assertEquals(REFRESH_TOKEN, result.getRefreshToken());
        assertEquals(authenticationRequest.getLogin().toLowerCase(), authenticationRequest.getLogin());

        verify(authenticationManager, times(1)).authenticate(usernamePasswordAuthenticationToken);
        verify(jwtService, times(1)).generateAccessToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
    }

    @Test
    public void auth_authException_throwExc() {
        var badCredentialsException = new BadCredentialsException("Bad credentials");
        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken))
                .thenThrow(badCredentialsException);

        BadCredentialsException result = assertThrows(BadCredentialsException.class, () -> authenticationService.auth(authenticationRequest));

        assertEquals(badCredentialsException.getMessage(), result.getMessage());
        verify(authenticationManager, times(1)).authenticate(usernamePasswordAuthenticationToken);
        verifyNoInteractions(jwtService);
    }

    @Test
    public void auth_userNotFound_throwExc() {
        var userNotFoundException = new UserNotFoundException("User with login: " + authenticationRequest.getLogin() + " not found");
        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken))
                .thenThrow(userNotFoundException);

        UserNotFoundException result = assertThrows(UserNotFoundException.class, () -> authenticationService.auth(authenticationRequest));

        assertEquals(userNotFoundException.getMessage(), result.getMessage());
        verify(authenticationManager, times(1)).authenticate(usernamePasswordAuthenticationToken);
        verifyNoInteractions(jwtService);
    }



}