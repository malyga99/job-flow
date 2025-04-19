package com.jobflow.user_service.auth;

import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private AuthenticationRequest authenticationRequest;

    private static final String LOGIN = "ivanivanov@gmail.com";

    @BeforeEach
    public void setup() {
        initDb();
        authenticationRequest = new AuthenticationRequest(LOGIN, "abcde");
    }

    @Test
    public void auth_returnAuthResponse() {
        HttpEntity<AuthenticationRequest> request = createRequest(authenticationRequest);
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
        assertNotNull(responseBody.getAccessToken());

        String loginAccessToken = jwtService.extractLogin(responseBody.getAccessToken());
        String loginRefreshToken = jwtService.extractLogin(responseBody.getRefreshToken());
        assertEquals(LOGIN, loginAccessToken);
        assertEquals(LOGIN, loginRefreshToken);
    }

    @Test
    public void auth_invalidData_returnBadRequest() {
        AuthenticationRequest invalidRequest = new AuthenticationRequest("", "");
        HttpEntity<AuthenticationRequest> request = createRequest(invalidRequest);
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
        authenticationRequest.setLogin("not" + LOGIN);
        HttpEntity<AuthenticationRequest> request = createRequest(authenticationRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/auth",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("User with login: " + authenticationRequest.getLogin() + " not found", error.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), error.getStatus());
        assertNotNull(error.getTime());
    }


    @Test
    public void auth_wrongPassword_returnUnauthorized() {
        authenticationRequest.setPassword("incorrectPassword");
        HttpEntity<AuthenticationRequest> request = createRequest(authenticationRequest);
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

    private HttpEntity<AuthenticationRequest> createRequest(AuthenticationRequest authenticationRequest) {
        return new HttpEntity<>(authenticationRequest);
    }

    private void initDb() {
        userRepository.deleteAll();

        User user = new User(null, "Ivan", "Ivanov", LOGIN, passwordEncoder.encode("abcde"), Role.ROLE_USER);
        userRepository.save(user);
    }
}
