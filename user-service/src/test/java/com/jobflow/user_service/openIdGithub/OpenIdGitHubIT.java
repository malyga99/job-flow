package com.jobflow.user_service.openIdGithub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.auth.AuthenticationRequest;
import com.jobflow.user_service.auth.AuthenticationResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OpenIdGitHubIT extends BaseIT {

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

    private byte[] avatar;

    @BeforeEach
    public void setup() throws JOSEException {
        userRepository.deleteAll();

        openIdRequest = TestUtil.createOpenIdRequest();
        openIdRequest.setProvider(AuthProvider.GITHUB);
        openIdRequest.setState(openIdProperties.getState());

        user = TestUtil.createUser();

        avatar = new byte[] {1,2,3};
    }

    @Test
    public void getJwtTokens_returnOpenIdResponse() {
        ResponseEntity<String> responseAccessToken = ResponseEntity.ok("{\"access_token\": \"" + TestUtil.ACCESS_TOKEN + "\"}");
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

        ResponseEntity<byte[]> responseAvatar = ResponseEntity.ok(avatar);
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
        assertEquals(TestUtil.AUTH_PROVIDER_ID, savedUser.getAuthProviderId());
    }

    @Test
    public void getJwtTokens_userAlreadyExists_returnJwtTokenAndDoesNotCreateUser() {
        User user = TestUtil.createUser();
        user.setAuthProvider(AuthProvider.GITHUB);
        user.setAuthProviderId(TestUtil.AUTH_PROVIDER_ID);
        user.setId(null);

        User savedUser = userRepository.save(user);

        ResponseEntity<String> responseAccessToken = ResponseEntity.ok("{\"access_token\": \"" + TestUtil.ACCESS_TOKEN + "\"}");
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

        ResponseEntity<byte[]> responseAvatar = ResponseEntity.ok(avatar);
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

    private String generateUserData() {
        return """
                {
                "name": "Ivan",
                "id": %s,
                "avatar_url": "some_url"
                }
                """.formatted(TestUtil.AUTH_PROVIDER_ID);
    }

}
