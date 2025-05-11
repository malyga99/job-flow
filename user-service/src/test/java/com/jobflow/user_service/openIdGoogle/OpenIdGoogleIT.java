package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.openId.OpenIdCacheService;
import com.jobflow.user_service.openId.OpenIdRequest;
import com.jobflow.user_service.openId.OpenIdResponse;
import com.jobflow.user_service.user.AuthProvider;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OpenIdGoogleIT extends BaseIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GoogleOpenIdProperties openIdProperties;

    @MockitoBean
    private OpenIdCacheService openIdCacheService;

    @MockitoBean
    private RestTemplate restTemplate;

    private JWKSet mockJwkSet;

    private RSAKey mockRsaKey;

    private String mockIdToken;

    private OpenIdRequest openIdRequest;

    @BeforeEach
    public void setup() throws JOSEException {
        userRepository.deleteAll();
        mockJwkSet = generateMockJwkSet();
        mockRsaKey = (RSAKey) mockJwkSet.getKeys().get(0);
        mockIdToken = generateJwt(mockRsaKey);

        openIdRequest = TestUtil.createOpenIdRequest();
        openIdRequest.setState(openIdProperties.getState());
    }

    @Test
    public void getJwtTokens_returnOpenIdResponse() {
        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + mockIdToken + "\"}");
        when(restTemplate.postForEntity(
                eq("https://oauth2.googleapis.com/token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(openIdCacheService.getJwkSet()).thenReturn(mockJwkSet);

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
        assertEquals("Ivanov", savedUser.getLastname());
        assertNull(savedUser.getLogin());
        assertNull(savedUser.getPassword());
        assertNull(savedUser.getAvatar());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
        assertEquals(AuthProvider.GOOGLE, savedUser.getAuthProvider());
        assertEquals(TestUtil.AUTH_PROVIDER_ID, savedUser.getAuthProviderId());
    }


    @Test
    public void getJwtTokens_userAlreadyExists_returnJwtTokenAndDoesNotCreateUser()  {
        User user = TestUtil.createUser();
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setAuthProviderId(TestUtil.AUTH_PROVIDER_ID);
        user.setId(null);

        User savedUser = userRepository.save(user);

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + mockIdToken + "\"}");
        when(restTemplate.postForEntity(
                eq("https://oauth2.googleapis.com/token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(openIdCacheService.getJwkSet()).thenReturn(mockJwkSet);

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
    public void getJwtTokens_invalidKeyId_returnBadRequest() throws JOSEException {
        RSAKey wrongRsaKey = new RSAKeyGenerator(2048).keyID("wrong-key-id").generate();
        String invalidIdToken = generateJwt(wrongRsaKey);

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + invalidIdToken + "\"}");
        when(restTemplate.postForEntity(
                eq("https://oauth2.googleapis.com/token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(openIdCacheService.getJwkSet()).thenReturn(mockJwkSet);

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
        assertEquals("Id token not valid", error.getMessage());
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());

        assertEquals(0, userRepository.count());
    }

    private JWKSet generateMockJwkSet() throws JOSEException {
        RSAKey mockRsaKey = new RSAKeyGenerator(2048)
                .keyID("test-key-id")
                .generate();

        return new JWKSet(mockRsaKey);
    }

    private String generateJwt(RSAKey mockRsaKey) throws JOSEException {
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(TestUtil.USER_ID)
                .issuer("https://accounts.google.com")
                .audience(openIdProperties.getClientId())
                .claim("given_name", "Ivan")
                .claim("family_name", "Ivanov")
                .claim("sub", TestUtil.AUTH_PROVIDER_ID)
                .claim("picture", null)
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 * 10))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(mockRsaKey.getKeyID()).build(),
                jwtClaimsSet
        );

        signedJWT.sign(new RSASSASigner(mockRsaKey));

        return signedJWT.serialize();
    }
}
