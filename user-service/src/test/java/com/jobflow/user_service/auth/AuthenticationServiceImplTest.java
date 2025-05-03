package com.jobflow.user_service.auth;

import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.TokenRevokedException;
import com.jobflow.user_service.exception.UserNotFoundException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
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
import java.util.Optional;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;

    private AuthenticationRequest authenticationRequest;

    private LogoutRequest logoutRequest;

    private RefreshTokenRequest refreshTokenRequest;

    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;


    @BeforeEach
    public void setup() {
        authenticationRequest = TestUtil.createAuthRequest();

        logoutRequest = TestUtil.createLogoutRequest();

        refreshTokenRequest = TestUtil.createRefreshRequest();

        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                authenticationRequest.getLogin(),
                authenticationRequest.getPassword()
        );
        user = TestUtil.createUser();
    }

    @Test
    public void auth_returnAuthResponse() {
        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn(TestUtil.ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(user)).thenReturn(TestUtil.REFRESH_TOKEN);

        AuthenticationResponse result = authenticationService.auth(authenticationRequest);

        assertNotNull(result);
        assertEquals(TestUtil.ACCESS_TOKEN, result.getAccessToken());
        assertEquals(TestUtil.REFRESH_TOKEN, result.getRefreshToken());
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
        verifyNoInteractions(jwtService);
    }

    @Test
    public void auth_userNotFound_throwExc() {
        var userNotFoundException = new UserNotFoundException("User with login: " + authenticationRequest.getLogin() + " not found");
        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken))
                .thenThrow(userNotFoundException);

        UserNotFoundException result = assertThrows(UserNotFoundException.class, () -> authenticationService.auth(authenticationRequest));

        assertEquals(userNotFoundException.getMessage(), result.getMessage());
        verifyNoInteractions(jwtService);
    }

    @Test
    public void logout_successfullyRevokeToken() {
        Date expiration = Date.from(Instant.now().plusSeconds(3600L));
        String tokenId = "token-id";
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(jwtService.extractClaims(logoutRequest.getRefreshToken())).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(expiration);
        when(claims.getId()).thenReturn(tokenId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authenticationService.logout(logoutRequest);

        verify(jwtService, times(1)).extractClaims(logoutRequest.getRefreshToken());
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
        when(jwtService.extractClaims(logoutRequest.getRefreshToken())).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(expiration);
        when(claims.getId()).thenReturn(tokenId);

        authenticationService.logout(logoutRequest);

        verifyNoInteractions(redisTemplate, valueOperations);
    }

    @Test
    public void refresh_returnAccessToken() {
        String tokenId = "token-id";
        when(jwtService.extractClaims(refreshTokenRequest.getRefreshToken())).thenReturn(claims);
        when(claims.getId()).thenReturn(tokenId);
        when(redisTemplate.hasKey("blacklist:refresh:token-id")).thenReturn(Boolean.FALSE);
        when(claims.getSubject()).thenReturn(TestUtil.LOGIN);
        when(userRepository.findByLogin(TestUtil.LOGIN)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn(TestUtil.ACCESS_TOKEN);

        String result = authenticationService.refreshToken(refreshTokenRequest);

        assertNotNull(result);
        assertEquals(TestUtil.ACCESS_TOKEN, result);

        verify(userRepository, times(1)).findByLogin(TestUtil.LOGIN);
        verify(jwtService, times(1)).generateAccessToken(user);
    }
    @Test
    public void refresh_ifRevoked_throwExc() {
        String tokenId = "token-id";
        when(jwtService.extractClaims(refreshTokenRequest.getRefreshToken())).thenReturn(claims);
        when(claims.getId()).thenReturn(tokenId);
        when(redisTemplate.hasKey("blacklist:refresh:token-id")).thenReturn(Boolean.TRUE);

        TokenRevokedException tokenRevokedException = assertThrows(TokenRevokedException.class, () -> authenticationService.refreshToken(refreshTokenRequest));
        assertEquals("Token with id: token-id revoked", tokenRevokedException.getMessage());

        verify(jwtService, never()).generateAccessToken(user);
    }

    @Test
    public void refresh_userNotFound_throwExc() {
        String tokenId = "token-id";
        when(jwtService.extractClaims(refreshTokenRequest.getRefreshToken())).thenReturn(claims);
        when(claims.getId()).thenReturn(tokenId);
        when(redisTemplate.hasKey("blacklist:refresh:token-id")).thenReturn(Boolean.FALSE);
        when(claims.getSubject()).thenReturn(TestUtil.LOGIN);
        when(userRepository.findByLogin(TestUtil.LOGIN)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> authenticationService.refreshToken(refreshTokenRequest));
        assertEquals("User with login: " + TestUtil.LOGIN + " not found", userNotFoundException.getMessage());

        verify(jwtService, never()).generateAccessToken(user);

    }


}