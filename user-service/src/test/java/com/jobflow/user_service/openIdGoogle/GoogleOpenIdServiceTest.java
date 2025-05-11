package com.jobflow.user_service.openIdGoogle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.openId.*;
import com.jobflow.user_service.user.User;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.org.yaml.snakeyaml.events.Event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOpenIdServiceTest {

    private static final String ID_TOKEN = "test-id-token";
    private static final String GRANT_TYPE = "test-grant-type";

    @Mock
    private OpenIdStateValidator openIdStateValidator;

    @Mock
    private OpenIdTokenValidator openIdTokenValidator;

    @Mock
    private GoogleOpenIdProperties openIdProperties;

    @Mock
    private OpenIdUserService openIdUserService;

    @Mock
    private OpenIdDataExtractor<JWTClaimsSet> openIdDataExtractor;

    @Mock
    private JwtService jwtService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SignedJWT signedJWT;

    @Mock
    private JWTClaimsSet claims;

    @Mock
    private JsonNode responseJsonNode;

    @Mock
    private JsonNode idTokenNode;

    @Spy
    @InjectMocks
    private GoogleOpenIdService openIdService;

    private OpenIdRequest openIdRequest;

    private OpenIdUserInfo userInfo;

    private User user;

    @BeforeEach
    public void setup() {
        openIdRequest = TestUtil.createOpenIdRequest();

        userInfo = TestUtil.createOpenIdUserInfo();
        user = TestUtil.createUser();
    }

    @Test
    public void getJwtTokens_returnOpenIdResponse() {
        try (MockedStatic<OpenIdJwtUtils> mockedStatic = mockStatic(OpenIdJwtUtils.class)) {
            doNothing().when(openIdStateValidator).validateState(openIdRequest.getState());
            doNothing().when(openIdTokenValidator).validateIdToken(signedJWT);
            doReturn(ID_TOKEN).when(openIdService).exchangeAuthCode(openIdRequest.getAuthCode());

            mockedStatic.when(() -> OpenIdJwtUtils.getJwt(ID_TOKEN)).thenReturn(signedJWT);
            mockedStatic.when(() -> OpenIdJwtUtils.extractClaims(signedJWT)).thenReturn(claims);

            when(openIdDataExtractor.extractUserInfo(claims)).thenReturn(userInfo);
            when(openIdUserService.getOrCreateUser(userInfo)).thenReturn(user);
            when(jwtService.generateAccessToken(user)).thenReturn(TestUtil.ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(user)).thenReturn(TestUtil.REFRESH_TOKEN);

            OpenIdResponse result = openIdService.getJwtTokens(openIdRequest);
            assertNotNull(result);
            assertEquals(TestUtil.ACCESS_TOKEN, result.getAccessToken());
            assertEquals(TestUtil.REFRESH_TOKEN, result.getRefreshToken());

            verify(openIdStateValidator, times(1)).validateState(openIdRequest.getState());
            verify(openIdTokenValidator, times(1)).validateIdToken(signedJWT);
            verify(openIdService, times(1)).exchangeAuthCode(openIdRequest.getAuthCode());
            verify(jwtService, times(1)).generateAccessToken(user);
            verify(jwtService, times(1)).generateRefreshToken(user);
        }
    }

    @Test
    public void exchangeAuthCode_returnIdToken() {
        var argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ResponseEntity<String> response = new ResponseEntity<>(ID_TOKEN, HttpStatus.OK);
        when(restTemplate.postForEntity(
                eq("https://oauth2.googleapis.com/token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);
        doReturn(ID_TOKEN).when(openIdService).extractToken(response.getBody());

        when(openIdProperties.getClientId()).thenReturn(TestUtil.CLIENT_ID);
        when(openIdProperties.getClientSecret()).thenReturn(TestUtil.CLIENT_SECRET);
        when(openIdProperties.getRedirectUri()).thenReturn(TestUtil.REDIRECT_URI);
        when(openIdProperties.getGrantType()).thenReturn(GRANT_TYPE);

        String result = openIdService.exchangeAuthCode(TestUtil.AUTH_CODE);

        assertNotNull(result);
        assertEquals(ID_TOKEN, result);

        verify(restTemplate, times(1)).postForEntity(
                eq("https://oauth2.googleapis.com/token"),
                argumentCaptor.capture(),
                eq(String.class)
        );

        HttpEntity<MultiValueMap<String, String>> httpEntity = argumentCaptor.getValue();
        MultiValueMap<String, String> body = httpEntity.getBody();
        HttpHeaders headers = httpEntity.getHeaders();

        assertEquals(TestUtil.CLIENT_ID, body.getFirst("client_id"));
        assertEquals(TestUtil.CLIENT_SECRET, body.getFirst("client_secret"));
        assertEquals(TestUtil.REDIRECT_URI, body.getFirst("redirect_uri"));
        assertEquals(GRANT_TYPE, body.getFirst("grant_type"));
        assertEquals(TestUtil.AUTH_CODE, body.getFirst("code"));
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
    }

    @Test
    public void extractToken_returnToken() throws JsonProcessingException {
        when(objectMapper.readTree(ID_TOKEN)).thenReturn(responseJsonNode);
        when(responseJsonNode.get("id_token")).thenReturn(idTokenNode);
        when(idTokenNode.asText()).thenReturn(ID_TOKEN);

        String result = openIdService.extractToken(ID_TOKEN);
        assertNotNull(result);
        assertEquals(ID_TOKEN, result);
    }

    @Test
    public void extractToken_tokenNotFound_throwExc() throws JsonProcessingException {
        when(objectMapper.readTree(ID_TOKEN)).thenReturn(responseJsonNode);
        when(responseJsonNode.get("id_token")).thenReturn(null);

        var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> openIdService.extractToken(ID_TOKEN));
        assertEquals("Id token not found in the response body", openIdServiceException.getMessage());
    }

    @Test
    public void extractToken_readNodeFailed_throwExc() throws JsonProcessingException {
        JsonProcessingException jsonProcessingException = new JsonProcessingException("Json exception"){};
        when(objectMapper.readTree(ID_TOKEN)).thenThrow(jsonProcessingException);

        var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> openIdService.extractToken(ID_TOKEN));
        assertEquals("Extract id token exception: " + jsonProcessingException.getMessage(), openIdServiceException.getMessage());
    }


}