package com.jobflow.user_service.auth;

import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.UserNotFoundException;
import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserDetailsService userDetailsService;

    private AuthenticationRequest authenticationRequest;

    private User savedUser;

    @BeforeEach
    public void setup() {
        initDb();
        clearRateLimitKeys();

        savedUser = userRepository.findAll().get(0);

        authenticationRequest = TestUtil.createAuthRequest();
    }

    @Test
    public void auth_returnAuthResponse() {
        HttpEntity<AuthenticationRequest> request = TestUtil.createRequest(authenticationRequest);
        ResponseEntity<AuthenticationResponse> response = restTemplate.exchange(
                "/api/v1/auth",
                HttpMethod.POST,
                request,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        AuthenticationResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getAccessToken());
        assertNotNull(responseBody.getRefreshToken());

        String userIdAccessToken = jwtService.extractUserId(responseBody.getAccessToken());
        String userIdRefreshToken = jwtService.extractUserId(responseBody.getRefreshToken());
        assertEquals(savedUser.getId(), Long.valueOf(userIdAccessToken));
        assertEquals(savedUser.getId(), Long.valueOf(userIdRefreshToken));
    }

    @Test
    public void auth_userNotFound_returnNotFound() {
        authenticationRequest.setLogin("incorrectLogin");
        HttpEntity<AuthenticationRequest> request = TestUtil.createRequest(authenticationRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("User with login: " + authenticationRequest.getLogin().toLowerCase() + " not found", error.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), error.getStatus());
        assertNotNull(error.getTime());
    }


    @Test
    public void auth_wrongPassword_returnUnauthorized() {
        authenticationRequest.setPassword("incorrectPassword");
        HttpEntity<AuthenticationRequest> request = TestUtil.createRequest(authenticationRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Bad credentials", error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void auth_tooManyRequests_returnTooManyRequests() {
        HttpEntity<AuthenticationRequest> request = TestUtil.createRequest(authenticationRequest);
        for (int i = 0; i < 5; i++) {
            restTemplate.exchange(
                    "/api/v1/auth",
                    HttpMethod.POST,
                    request,
                    AuthenticationResponse.class
            );
        }

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Too many login attempts. Try again in a minute", error.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void logout_successfullyRevokeToken() {
        AuthenticationResponse authenticationResponse = authenticateUser();
        String refreshToken = authenticationResponse.getRefreshToken();
        String accessToken = authenticationResponse.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);
        HttpEntity<LogoutRequest> request = TestUtil.createRequest(logoutRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                request,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String tokenId = jwtService.extractClaims(refreshToken).getId();
        String key = String.format("blacklist:refresh:%s", tokenId);
        String value = redisTemplate.opsForValue().get(key);

        assertNotNull(value);
        assertEquals("true", value);
    }

    @Test
    public void logout_expiredRefreshToken_doesNotRevoke() {
        AuthenticationResponse authenticationResponse = authenticateUser();
        String refreshToken = generateExpiredRefreshToken();
        String accessToken = authenticationResponse.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);
        HttpEntity<LogoutRequest> request = TestUtil.createRequest(logoutRequest, headers);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void logout_withoutToken_returnUnauthorized() {
        AuthenticationResponse authenticationResponse = authenticateUser();
        String refreshToken = authenticationResponse.getRefreshToken();

        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);
        HttpEntity<LogoutRequest> request = TestUtil.createRequest(logoutRequest);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void logout_tooManyRequests_returnTooManyRequests() {
        AuthenticationResponse authenticationResponse = authenticateUser();
        String refreshToken = authenticationResponse.getRefreshToken();
        String accessToken = authenticationResponse.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);
        HttpEntity<LogoutRequest> request = TestUtil.createRequest(logoutRequest, headers);

        for (int i = 0; i < 5; i++) {
            restTemplate.exchange(
                    "/api/v1/auth/logout",
                    HttpMethod.POST,
                    request,
                    Void.class
            );
        }

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Too many logout attempts. Try again in a minute", error.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void refresh_returnAccessToken() {
        AuthenticationResponse authenticationResponse = authenticateUser();
        String refreshToken = authenticationResponse.getRefreshToken();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
        HttpEntity<RefreshTokenRequest> request = TestUtil.createRequest(refreshTokenRequest);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/auth/refresh",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String refreshedToken = response.getBody();
        assertNotNull(refreshedToken);

        String refreshedTokenUserId = jwtService.extractUserId(refreshedToken);
        assertEquals(savedUser.getId(), Long.valueOf(refreshedTokenUserId));
    }

    @Test
    public void refresh_tokenRevoked_returnUnauthorized() {
        AuthenticationResponse authenticationResponse = authenticateUser();
        String refreshToken = authenticationResponse.getRefreshToken();

        String refreshTokenId = jwtService.extractClaims(refreshToken).getId();
        revokeToken(refreshTokenId);

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
        HttpEntity<RefreshTokenRequest> request = TestUtil.createRequest(refreshTokenRequest);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth/refresh",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void refresh_expiredRefreshToken_doesNotRefresh() {
        String refreshToken = generateExpiredRefreshToken();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
        HttpEntity<RefreshTokenRequest> request = TestUtil.createRequest(refreshTokenRequest);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth/refresh",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void refresh_tooManyRequests_returnTooManyRequests() {
        AuthenticationResponse authenticationResponse = authenticateUser();
        String refreshToken = authenticationResponse.getRefreshToken();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
        HttpEntity<RefreshTokenRequest> request = TestUtil.createRequest(refreshTokenRequest);

        for (int i = 0; i < 5; i++) {
            restTemplate.exchange(
                    "/api/v1/auth/refresh",
                    HttpMethod.POST,
                    request,
                    Void.class
            );
        }

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth/refresh",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Too many token refresh attempts. Try again in a minute", error.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    //For JaCoCo code coverage
    @Test
    public void loadUserByUsername_idNotFound_returnNotFound() {
        var userNotFoundException = assertThrows(UserNotFoundException.class, () -> userDetailsService.loadUserByUsername("999"));

        assertEquals("User with id: 999 not found", userNotFoundException.getMessage());
    }

    private AuthenticationResponse authenticateUser() {
        HttpEntity<AuthenticationRequest> request = TestUtil.createRequest(authenticationRequest);
        ResponseEntity<AuthenticationResponse> response = restTemplate.exchange(
                "/api/v1/auth",
                HttpMethod.POST,
                request,
                AuthenticationResponse.class
        );

        return response.getBody();
    }

    private String generateExpiredRefreshToken() {
        return Jwts.builder()
                .setSubject(String.valueOf(savedUser.getId()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() - 1000L))
                .signWith(jwtService.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private void revokeToken(String refreshTokenId) {
        redisTemplate.opsForValue().set(
                "blacklist:refresh:" + refreshTokenId,
                "true",
                Duration.ofMinutes(1L));
    }

    private void initDb() {
        userRepository.deleteAll();

        User user = TestUtil.createUser();

        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    private void clearRateLimitKeys() {
        Set<String> keys = redisTemplate.keys("rate_limiter:*");
        redisTemplate.delete(keys);
    }
}
