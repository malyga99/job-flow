package com.jobflow.user_service.openIdGithub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.openId.OpenIdRequest;
import com.jobflow.user_service.openId.OpenIdResponse;
import com.jobflow.user_service.user.AuthProvider;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OpenIdGitHubIT extends BaseIT {

    private static final String ACCESS_TOKEN = "access_token";

    private OpenIdRequest openIdRequest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GitHubOpenIdProperties openIdProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RestTemplate restTemplate;

    private User user;

    @BeforeEach
    public void setup() throws JOSEException {
        userRepository.deleteAll();

        openIdRequest = new OpenIdRequest(AuthProvider.GITHUB, openIdProperties.getState(), TestUtil.AUTH_CODE);

        user = TestUtil.createUser();
    }

    @Test
    public void getJwtTokens_returnOpenIdResponse() {
        byte[] mockAvatar = new byte[]{1, 2, 3};
        ResponseEntity<String> responseAccessToken = ResponseEntity.ok("{\"access_token\": \"" + ACCESS_TOKEN + "\"}");
        when(restTemplate.postForEntity(
                eq("https://github.com/login/oauth/access_token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseAccessToken);

        String userData = generateUserData();
        ResponseEntity<String> responseUserData = ResponseEntity.ok(userData);
        when(restTemplate.exchange(
                eq("https://api.github.com/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseUserData);

        ResponseEntity<byte[]> responseAvatar = ResponseEntity.ok(mockAvatar);
        when(restTemplate.getForEntity(eq("some_url"), eq(byte[].class)))
                .thenReturn(responseAvatar);

        HttpEntity<OpenIdRequest> request = TestUtil.createRequest(openIdRequest);
        ResponseEntity<OpenIdResponse> response = testRestTemplate.exchange(
                "/api/v1/openid",
                HttpMethod.POST,
                request,
                OpenIdResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        OpenIdResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getAccessToken());
        assertNotNull(responseBody.getRefreshToken());

        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());

        User savedUser = users.get(0);

        String userIdAccessToken = jwtService.extractUserId(responseBody.getAccessToken());
        String userIdRefreshToken = jwtService.extractUserId(responseBody.getRefreshToken());
        assertEquals(savedUser.getId(), Long.valueOf(userIdAccessToken));
        assertEquals(savedUser.getId(), Long.valueOf(userIdRefreshToken));

        assertEquals("Ivan", savedUser.getFirstname());
        assertNull(savedUser.getPassword());
        assertNull(savedUser.getLogin());
        assertNull(savedUser.getLastname());
        assertNotNull(savedUser.getAvatar());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
        assertEquals(AuthProvider.GITHUB, savedUser.getAuthProvider());
        assertEquals("123", savedUser.getAuthProviderId());
    }

    @Test
    public void getJwtTokens_userAlreadyExists_returnJwtTokenAndDoesNotCreateUser() {
        User user = TestUtil.createUser();
        user.setAuthProvider(AuthProvider.GITHUB);
        user.setAuthProviderId("123");
        user.setId(null);

        User savedUser = userRepository.save(user);

        byte[] mockAvatar = new byte[]{1, 2, 3};
        ResponseEntity<String> responseAccessToken = ResponseEntity.ok("{\"access_token\": \"" + ACCESS_TOKEN + "\"}");
        when(restTemplate.postForEntity(
                eq("https://github.com/login/oauth/access_token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseAccessToken);

        String userData = generateUserData();
        ResponseEntity<String> responseUserData = ResponseEntity.ok(userData);
        when(restTemplate.exchange(
                eq("https://api.github.com/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseUserData);

        ResponseEntity<byte[]> responseAvatar = ResponseEntity.ok(mockAvatar);
        when(restTemplate.getForEntity(eq("some_url"), eq(byte[].class)))
                .thenReturn(responseAvatar);

        HttpEntity<OpenIdRequest> request = TestUtil.createRequest(openIdRequest);
        ResponseEntity<OpenIdResponse> response = testRestTemplate.exchange(
                "/api/v1/openid",
                HttpMethod.POST,
                request,
                OpenIdResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        OpenIdResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getAccessToken());
        assertNotNull(responseBody.getRefreshToken());

        String userIdAccessToken = jwtService.extractUserId(responseBody.getAccessToken());
        String userIdRefreshToken = jwtService.extractUserId(responseBody.getRefreshToken());
        assertEquals(savedUser.getId(), Long.valueOf(userIdAccessToken));
        assertEquals(savedUser.getId(), Long.valueOf(userIdRefreshToken));

        assertEquals(1, userRepository.count());
    }

    @Test
    public void getJwtTokens_invalidState_returnBadRequest() {
        openIdRequest.setState("invalidState");

        HttpEntity<OpenIdRequest> request = TestUtil.createRequest(openIdRequest);
        ResponseEntity<ResponseError> response = testRestTemplate.exchange(
                "/api/v1/openid",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("State: " + openIdRequest.getState() + " not valid", error.getMessage());
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());

        assertEquals(0, userRepository.count());
    }

    @Test
    public void getJwtTokens_invalidData_returnBadRequest() {
        OpenIdRequest invalidRequest = new OpenIdRequest(AuthProvider.GITHUB, "", "");
        HttpEntity<OpenIdRequest> request = TestUtil.createRequest(invalidRequest);
        ResponseEntity<ResponseError> response = testRestTemplate.exchange(
                "/api/v1/openid",
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

    private String generateUserData() {
        return """
                {
                "name": "Ivan",
                "id": "123",
                "avatar_url": "some_url"
                }
                """;
    }

}
