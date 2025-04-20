package com.jobflow.user_service.auth;

import com.jobflow.user_service.exception.TokenRevokedException;
import com.jobflow.user_service.exception.UserNotFoundException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @Mock
    private UserService userService;

    @Mock
    private Claims claims;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;

    private AuthenticationRequest authenticationRequest;

    private LogoutRequest logoutRequest;

    private RefreshTokenRequest refreshTokenRequest;

    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

    private static final String ACCESS_TOKEN = "my.access.token";

    private static final String REFRESH_TOKEN = "my.refresh.token";

    @BeforeEach
    public void setup() {
        authenticationRequest = new AuthenticationRequest("IvanIvanov@gmail.com", "abcde");
        logoutRequest = new LogoutRequest(REFRESH_TOKEN);
        refreshTokenRequest = new RefreshTokenRequest(REFRESH_TOKEN);
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

    @Test
    public void logout_successfullyRevokeToken() {
        Date expiration = Date.from(Instant.now().plusSeconds(3600L));
        String tokenId = "token-id";
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(jwtService.extractClaims(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(expiration);
        when(claims.getId()).thenReturn(tokenId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authenticationService.logout(logoutRequest);

        verify(jwtService, times(1)).extractClaims(REFRESH_TOKEN);
        verify(valueOperations, times(1)).set(
                eq("blacklist:refresh:token-id"),
                eq("true"),
                ttlCaptor.capture(),
                eq(TimeUnit.SECONDS)
        );

        Long ttl = ttlCaptor.getValue();
        assertTrue(ttl >= 3595L && ttl <= 3600L);
    }

    @Test
    public void logout_expiredToken_skipBlacklist() {
        Date expiration = Date.from(Instant.now().minusSeconds(60L));
        String tokenId = "token-id";

        when(userService.getCurrentUser()).thenReturn(user);
        when(jwtService.extractClaims(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(expiration);
        when(claims.getId()).thenReturn(tokenId);

        authenticationService.logout(logoutRequest);

        verify(jwtService, times(1)).extractClaims(REFRESH_TOKEN);
        verifyNoInteractions(redisTemplate, valueOperations);
    }

    @Test
    public void refresh_returnAccessToken() {
        String tokenId = "token-id";
        when(userService.getCurrentUser()).thenReturn(user);
        when(jwtService.extractClaims(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.getId()).thenReturn(tokenId);
        when(redisTemplate.hasKey("blacklist:refresh:token-id")).thenReturn(Boolean.FALSE);
        when(jwtService.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);

        String result = authenticationService.refreshToken(refreshTokenRequest);

        assertNotNull(result);
        assertEquals(ACCESS_TOKEN, result);

        verify(jwtService, times(1)).generateAccessToken(user);
    }
    @Test
    public void refresh_ifRevoked_throwExc() {
        String tokenId = "token-id";
        when(userService.getCurrentUser()).thenReturn(user);
        when(jwtService.extractClaims(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.getId()).thenReturn(tokenId);
        when(redisTemplate.hasKey("blacklist:refresh:token-id")).thenReturn(Boolean.TRUE);

        TokenRevokedException tokenRevokedException = assertThrows(TokenRevokedException.class, () -> authenticationService.refreshToken(refreshTokenRequest));
        assertEquals("Token with id: token-id revoked", tokenRevokedException.getMessage());

        verify(jwtService, never()).generateAccessToken(user);
    }


}