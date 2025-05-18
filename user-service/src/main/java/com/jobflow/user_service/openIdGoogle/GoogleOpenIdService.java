package com.jobflow.user_service.openIdGoogle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.openId.*;
import com.jobflow.user_service.rateLimiter.RateLimiterKeyUtil;
import com.jobflow.user_service.rateLimiter.RateLimiterService;
import com.jobflow.user_service.user.AuthProvider;
import com.jobflow.user_service.user.User;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class GoogleOpenIdService implements OpenIdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOpenIdService.class);

    private final OpenIdStateValidator openIdStateValidator;
    private final OpenIdTokenValidator openIdTokenValidator;
    private final OpenIdDataExtractor<JWTClaimsSet> openIdDataExtractor;
    private final OpenIdUserService openIdUserService;
    private final GoogleOpenIdProperties openIdProperties;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public GoogleOpenIdService(
            @Qualifier("googleOpenIdStateValidator") OpenIdStateValidator openIdStateValidator,
            @Qualifier("googleOpenIdTokenValidator") OpenIdTokenValidator openIdTokenValidator,
            @Qualifier("googleOpenIdDataExtractor") OpenIdDataExtractor<JWTClaimsSet> openIdDataExtractor,
            OpenIdUserService openIdUserService,
            GoogleOpenIdProperties openIdProperties,
            JwtService jwtService,
            RestTemplate restTemplate,
            RateLimiterService rateLimiterService,
            ObjectMapper objectMapper) {
        this.openIdStateValidator = openIdStateValidator;
        this.openIdTokenValidator = openIdTokenValidator;
        this.openIdDataExtractor = openIdDataExtractor;
        this.openIdUserService = openIdUserService;
        this.openIdProperties = openIdProperties;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    public OpenIdResponse getJwtTokens(OpenIdRequest openIdRequest, String clientIp) {
        LOGGER.info("Starting Google OpenID authentication");
        String state = openIdRequest.getState();
        String authCode = openIdRequest.getAuthCode();

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateIpKey("googleOpenId", clientIp),
                5,
                Duration.ofMinutes(1),
                "Too many OpenID attempts. Try again in a minute"
        );

        openIdStateValidator.validateState(state);

        String idToken = exchangeAuthCode(authCode);

        SignedJWT signedJWT = OpenIdJwtUtils.getJwt(idToken);
        openIdTokenValidator.validateIdToken(signedJWT);

        JWTClaimsSet claims = OpenIdJwtUtils.extractClaims(signedJWT);
        OpenIdUserInfo userInfo = openIdDataExtractor.extractUserInfo(claims);

        User user = openIdUserService.getOrCreateUser(userInfo);

        LOGGER.info("Successfully Google OpenID authentication for user: {}", user.displayInfo());
        return new OpenIdResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    @Override
    public String exchangeAuthCode(String authCode) {
        LOGGER.debug("Sending request to Google for exchange authCode");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authCode);
        body.add("client_id", openIdProperties.getClientId());
        body.add("client_secret", openIdProperties.getClientSecret());
        body.add("redirect_uri", openIdProperties.getRedirectUri());
        body.add("grant_type", openIdProperties.getGrantType());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                request,
                String.class
        );

        LOGGER.debug("Successfully received response from Google");
        return extractToken(response.getBody());
    }

    @Override
    public String extractToken(String response) {
        LOGGER.debug("Extracting id token from response body");
        JsonNode idTokenNode;
        try {
            JsonNode responseJsonNode = objectMapper.readTree(response);
            idTokenNode = responseJsonNode.get("id_token");
        } catch (JsonProcessingException e) {
            throw new OpenIdServiceException("Extract id token exception: " + e.getMessage(), e);
        }

        if (idTokenNode == null) {
            throw new OpenIdServiceException("Id token not found in the response body");
        }

        LOGGER.debug("Successfully extracted id token: {}", idTokenNode.asText());
        return idTokenNode.asText();
    }

    @Override
    public AuthProvider getProviderName() {
        return AuthProvider.GOOGLE;
    }

}
