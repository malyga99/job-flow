package com.jobflow.notification_service.user;

import com.jobflow.notification_service.exception.UserClientException;
import com.jobflow.notification_service.telegram.TelegramChatLinkRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class RestUserClient implements UserClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestUserClient.class);
    private static final String USER_SERVICE_URL = "http://%s:%s/api/v1/users";

    private final UserServiceProperties userServiceProperties;
    private final RestTemplate restTemplate;

    @Override
    public UserInfo getUserInfo(Long userId) {
        LOGGER.debug("Sending request to fetch user info by userId: {}", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", userServiceProperties.getApiKey());

        HttpEntity<Void> request = new HttpEntity<>(null, headers);
        String url = String.format(USER_SERVICE_URL, userServiceProperties.getHost(), userServiceProperties.getPort())
                     + "/info?userId=" + userId;

        try {
            ResponseEntity<UserInfo> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    UserInfo.class
            );

            LOGGER.debug("Successfully fetched user info by userId: {}", userId);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new UserClientException("Failed to fetch user info, status: " + e.getStatusCode(), e);
        }
    }

    @Override
    public void linkChatId(Long userId, Long chatId) {
        LOGGER.debug("Sending request to link chatId: {} and userId: {}", chatId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", userServiceProperties.getApiKey());

        TelegramChatLinkRequest linkRequest = TelegramChatLinkRequest.builder()
                .userId(userId)
                .chatId(chatId)
                .build();

        HttpEntity<TelegramChatLinkRequest> request = new HttpEntity<>(linkRequest, headers);
        String url = String.format(USER_SERVICE_URL, userServiceProperties.getHost(), userServiceProperties.getPort())
                     + "/telegram";

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Void.class
            );

            LOGGER.debug("Successfully sent link request for chatId: {} and userId: {}", chatId, userId);
        } catch (HttpStatusCodeException e) {
            throw new UserClientException("Failed to link telegram, status: " + e.getStatusCode(), e);
        }
    }
}
