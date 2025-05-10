package com.jobflow.user_service.openIdGithub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.openId.*;
import com.jobflow.user_service.user.AuthProvider;
import com.jobflow.user_service.user.User;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GitHubOpenIdService implements OpenIdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubOpenIdService.class);

    private final OpenIdStateValidator openIdStateValidator;
    private final OpenIdDataExtractor<String> openIdDataExtractor;
    private final OpenIdUserService openIdUserService;
    private final GitHubOpenIdProperties openIdProperties;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubOpenIdService(
            @Qualifier("gitHubOpenIdStateValidator") OpenIdStateValidator openIdStateValidator,
            @Qualifier("gitHubOpenIdDataExtractor") OpenIdDataExtractor<String> openIdDataExtractor,
            OpenIdUserService openIdUserService,
            GitHubOpenIdProperties openIdProperties,
            JwtService jwtService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.openIdStateValidator = openIdStateValidator;
        this.openIdDataExtractor = openIdDataExtractor;
        this.openIdUserService = openIdUserService;
        this.openIdProperties = openIdProperties;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public OpenIdResponse getJwtTokens(OpenIdRequest openIdRequest) {
        LOGGER.info("Starting GitHub OpenID authentication");
        String state = openIdRequest.getState();
        String authCode = openIdRequest.getAuthCode();

        openIdStateValidator.validateState(state);

        String accessToken = exchangeAuthCode(authCode);
        String userData = getUserData(accessToken);

        OpenIdUserInfo userInfo = openIdDataExtractor.extractUserInfo(userData);

        User user = openIdUserService.getOrCreateUser(userInfo);

        LOGGER.info("Successfully Github OpenID authentication for user: {}", user.displayInfo());
        return new OpenIdResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );

    }

    @Override
    public String exchangeAuthCode(String authCode) {
        LOGGER.debug("Sending request to GitHub for exchange authCode");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authCode);
        body.add("client_id", openIdProperties.getClientId());
        body.add("client_secret", openIdProperties.getClientSecret());
        body.add("redirect_uri", openIdProperties.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token",
                request,
                String.class
        );

        LOGGER.debug("Successfully received response from GitHub");
        return extractToken(response.getBody());
    }

    @Override
    public String extractToken(String response) {
        LOGGER.debug("Extracting access token from response body");
        JsonNode accessTokenNode;
        try {
            JsonNode responseJsonNode = objectMapper.readTree(response);
            accessTokenNode = responseJsonNode.get("access_token");
        } catch (JsonProcessingException e) {
            throw new OpenIdServiceException("Extract access token exception: " + e.getMessage(), e);
        }

        if (accessTokenNode == null) {
            throw new OpenIdServiceException("Access token not found in the response body");
        }

        LOGGER.debug("Successfully extracted access token: {}", accessTokenNode.asText());
        return accessTokenNode.asText();
    }

    @Override
    public AuthProvider getProviderName() {
        return AuthProvider.GITHUB;
    }

    private String getUserData(String accessToken) {
        LOGGER.debug("Sending request to GitHub for get user data");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                request,
                String.class
        );

        LOGGER.debug("Successfully received user data from GitHub");
        return response.getBody();
    }
}
