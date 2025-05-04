package com.jobflow.user_service.openIdGoogle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.openId.*;
import com.jobflow.user_service.user.User;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;

@Service
@RequiredArgsConstructor
public class GoogleOpenIdService implements OpenIdService {

    private final OpenIdStateValidator stateValidatorService;
    private final OpenIdTokenValidator tokenValidatorService;
    private final GoogleOpenIdProperties openIdProperties;
    private final OpenIdUserService openIdUserService;
    private final OpenIdDataExtractor<JWTClaimsSet> dataExtractor;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public OpenIdResponse getJwtTokens(OpenIdRequest openIdRequest) {
        String state = openIdRequest.getState();
        String authCode = openIdRequest.getAuthCode();

        stateValidatorService.validateState(state);

        String idToken = exchangeAuthCode(authCode);

        SignedJWT signedJWT = OpenIdJwtUtils.getJwt(idToken);
        tokenValidatorService.validateIdToken(signedJWT);

        JWTClaimsSet claims = OpenIdJwtUtils.extractClaims(signedJWT);
        OpenIdUserInfo userInfo = dataExtractor.extractUserInfo(claims);

        User user = openIdUserService.getOrCreateUser(userInfo);
        return new OpenIdResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    @Override
    public String exchangeAuthCode(String authCode) {
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

        return extractIdToken(response.getBody());
    }

    @Override
    public OpenIdProvider getProviderName() {
        return OpenIdProvider.GOOGLE;
    }

    private String extractIdToken(String response) {
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
        return idTokenNode.asText();
    }
}
