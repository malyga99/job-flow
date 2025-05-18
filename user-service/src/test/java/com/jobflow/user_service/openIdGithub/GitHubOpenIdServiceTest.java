package com.jobflow.user_service.openIdGithub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.exception.TooManyRequestsException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.openId.*;
import com.jobflow.user_service.rateLimiter.RateLimiterKeyUtil;
import com.jobflow.user_service.rateLimiter.RateLimiterService;
import com.jobflow.user_service.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubOpenIdServiceTest {

    @Mock
    private OpenIdStateValidator openIdStateValidator;

    @Mock
    private GitHubOpenIdProperties openIdProperties;

    @Mock
    private OpenIdUserService openIdUserService;

    @Mock
    private OpenIdDataExtractor<String> openIdDataExtractor;

    @Mock
    private JwtService jwtService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode responseJsonNode;

    @Mock
    private JsonNode idTokenNode;

    @Mock
    private RateLimiterService rateLimiterService;

    @Spy
    @InjectMocks
    private GitHubOpenIdService openIdService;

    private OpenIdRequest openIdRequest;

    private String expectedUserData;

    private OpenIdUserInfo userInfo;

    private User user;

    @BeforeEach
    public void setup() {
        openIdRequest = TestUtil.createOpenIdRequest();

        expectedUserData = "userData";
        userInfo = TestUtil.createOpenIdUserInfo();

        user = TestUtil.createUser();
    }

    @Test
    public void getJwtTokens_returnOpenIdResponse() {
        ResponseEntity<String> response = new ResponseEntity<>(expectedUserData, HttpStatus.OK);
        doNothing().when(openIdStateValidator).validateState(openIdRequest.getState());
        doReturn(TestUtil.ACCESS_TOKEN).when(openIdService).exchangeAuthCode(openIdRequest.getAuthCode());
        when(restTemplate.exchange(
                eq("https://api.github.com/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        when(openIdDataExtractor.extractUserInfo(expectedUserData)).thenReturn(userInfo);
        when(openIdUserService.getOrCreateUser(userInfo)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn(TestUtil.ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(user)).thenReturn(TestUtil.REFRESH_TOKEN);

        OpenIdResponse result = openIdService.getJwtTokens(openIdRequest, "test-ip");
        assertNotNull(result);
        assertEquals(TestUtil.ACCESS_TOKEN, result.getAccessToken());
        assertEquals(TestUtil.REFRESH_TOKEN, result.getRefreshToken());

        verify(openIdStateValidator, times(1)).validateState(openIdRequest.getState());
        verify(openIdService, times(1)).exchangeAuthCode(openIdRequest.getAuthCode());
        verify(jwtService, times(1)).generateAccessToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
    }

    @Test
    public void getJwtTokens_tooManyRequests_throwExc() {
        var tooManyRequestsException = new TooManyRequestsException("Too many OpenID attempts. Try again in a minute");
        doThrow(tooManyRequestsException).when(rateLimiterService).validateOrThrow(
                RateLimiterKeyUtil.generateIpKey("gitHubOpenId", "test-ip"),
                5,
                Duration.ofMinutes(1),
                "Too many OpenID attempts. Try again in a minute"
        );

        var result = assertThrows(TooManyRequestsException.class, () -> openIdService.getJwtTokens(openIdRequest, "test-ip"));
        assertEquals(tooManyRequestsException.getMessage(), result.getMessage());

        verifyNoInteractions(jwtService, openIdDataExtractor, openIdUserService);
    }

    @Test
    public void exchangeAuthCode_returnAccessToken() {
        var argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ResponseEntity<String> response = new ResponseEntity<>(TestUtil.ACCESS_TOKEN, HttpStatus.OK);
        when(restTemplate.postForEntity(
                eq("https://github.com/login/oauth/access_token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);
        doReturn(TestUtil.ACCESS_TOKEN).when(openIdService).extractToken(response.getBody());

        when(openIdProperties.getClientId()).thenReturn(TestUtil.CLIENT_ID);
        when(openIdProperties.getClientSecret()).thenReturn(TestUtil.CLIENT_SECRET);
        when(openIdProperties.getRedirectUri()).thenReturn(TestUtil.REDIRECT_URI);

        String result = openIdService.exchangeAuthCode(TestUtil.AUTH_CODE);

        assertNotNull(result);
        assertEquals(TestUtil.ACCESS_TOKEN, result);

        verify(restTemplate, times(1)).postForEntity(
                eq("https://github.com/login/oauth/access_token"),
                argumentCaptor.capture(),
                eq(String.class)
        );

        HttpEntity<MultiValueMap<String, String>> httpEntity = argumentCaptor.getValue();
        MultiValueMap<String, String> body = httpEntity.getBody();
        HttpHeaders headers = httpEntity.getHeaders();

        assertEquals(TestUtil.CLIENT_ID, body.getFirst("client_id"));
        assertEquals(TestUtil.CLIENT_SECRET, body.getFirst("client_secret"));
        assertEquals(TestUtil.REDIRECT_URI, body.getFirst("redirect_uri"));
        assertEquals(TestUtil.AUTH_CODE, body.getFirst("code"));
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
    }

    @Test
    public void extractToken_returnToken() throws JsonProcessingException {
        when(objectMapper.readTree(TestUtil.ACCESS_TOKEN)).thenReturn(responseJsonNode);
        when(responseJsonNode.get("access_token")).thenReturn(idTokenNode);
        when(idTokenNode.asText()).thenReturn(TestUtil.ACCESS_TOKEN);

        String result = openIdService.extractToken(TestUtil.ACCESS_TOKEN);
        assertNotNull(result);
        assertEquals(TestUtil.ACCESS_TOKEN, result);
    }

    @Test
    public void extractToken_tokenNotFound_throwExc() throws JsonProcessingException {
        when(objectMapper.readTree(TestUtil.ACCESS_TOKEN)).thenReturn(responseJsonNode);
        when(responseJsonNode.get("access_token")).thenReturn(null);

        var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> openIdService.extractToken(TestUtil.ACCESS_TOKEN));
        assertEquals("Access token not found in the response body", openIdServiceException.getMessage());
    }

    @Test
    public void extractToken_readNodeFailed_throwExc() throws JsonProcessingException {
        JsonProcessingException jsonProcessingException = new JsonProcessingException("Json exception"){};
        when(objectMapper.readTree(TestUtil.ACCESS_TOKEN)).thenThrow(jsonProcessingException);

        var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> openIdService.extractToken(TestUtil.ACCESS_TOKEN));
        assertEquals("Extract access token exception: " + jsonProcessingException.getMessage(), openIdServiceException.getMessage());
    }
}