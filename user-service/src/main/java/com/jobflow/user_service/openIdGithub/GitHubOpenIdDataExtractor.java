package com.jobflow.user_service.openIdGithub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdDataExtractor;
import com.jobflow.user_service.openId.OpenIdUserInfo;
import com.jobflow.user_service.user.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubOpenIdDataExtractor implements OpenIdDataExtractor<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubOpenIdDataExtractor.class);

    private final ObjectMapper objectMapper;

    @Override
    public String extractData(String data, String key) {
        LOGGER.debug("Extracting value: {} from json", key);
        try {
            JsonNode dataNode = objectMapper.readTree(data);
            JsonNode keyNode = dataNode.get(key);

            if (keyNode != null) {
                LOGGER.debug("Successfully extracted value: {} from json", key);
                return keyNode.asText();
            }

            return null;
        } catch (JsonProcessingException e) {
            throw new OpenIdServiceException("Failed to extract value: " + key + " from json: " + e.getMessage(), e);
        }
    }

    @Override
    public OpenIdUserInfo extractUserInfo(String data) {
        LOGGER.debug("Extracting user info from json");
        OpenIdUserInfo userInfo = OpenIdUserInfo.builder()
                .firstname(extractData(data, "name"))
                .authProvider(AuthProvider.GITHUB)
                .authProviderId(extractData(data, "id"))
                .avatarUrl(extractData(data, "avatar_url"))
                .build();
        LOGGER.debug("Successfully extracted user info from json. Provider id: {}", userInfo.getAuthProviderId());

        return userInfo;
    }
}
