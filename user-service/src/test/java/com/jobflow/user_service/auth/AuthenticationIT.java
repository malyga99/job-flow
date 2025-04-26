package com.jobflow.user_service.auth;

import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.Role;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    private AuthenticationRequest authenticationRequest;

    private static final String LOGIN = "ivanivanov@gmail.com";
    private static final String PASSWORD = "abcde";
    private static final String BLACKLIST_KEY = "blacklist:refresh:%s";

    @BeforeEach
    public void setup() {
        initDb();
        authenticationRequest = new AuthenticationRequest(LOGIN, PASSWORD);
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

        String loginAccessToken = jwtService.extractLogin(responseBody.getAccessToken());
        String loginRefreshToken = jwtService.extractLogin(responseBody.getRefreshToken());
        assertEquals(LOGIN, loginAccessToken);
        assertEquals(LOGIN, loginRefreshToken);
    }

    @Test
    public void auth_invalidData_returnBadRequest() {
        AuthenticationRequest invalidRequest = new AuthenticationRequest("", "");
        HttpEntity<AuthenticationRequest> request = TestUtil.createRequest(invalidRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());
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
        String key = String.format(BLACKLIST_KEY, tokenId);
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
    public void logout_invalidData_returnBadRequest() {
        AuthenticationResponse authenticationResponse = authenticateUser();
        String accessToken = authenticationResponse.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        LogoutRequest logoutRequest = new LogoutRequest(null);
        HttpEntity<LogoutRequest> request = TestUtil.createRequest(logoutRequest, headers);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());
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

        String refreshedTokenLogin = jwtService.extractLogin(refreshedToken);
        assertEquals(LOGIN, refreshedTokenLogin);
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
    public void refresh_invalidData_returnBadRequest() {

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(null);
        HttpEntity<RefreshTokenRequest> request = TestUtil.createRequest(refreshTokenRequest);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth/refresh",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());
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
                .setSubject(LOGIN)
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

        User user = new User(null, "Ivan", "Ivanov", LOGIN, passwordEncoder.encode(PASSWORD), Role.ROLE_USER);
        userRepository.save(user);
    }
}
