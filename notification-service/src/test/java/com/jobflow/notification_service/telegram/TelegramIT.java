package com.jobflow.notification_service.telegram;

import com.jobflow.notification_service.BaseIT;
import com.jobflow.notification_service.JwtTestUtil;
import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.handler.ResponseError;
import com.jobflow.notification_service.user.UserServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TelegramIT extends BaseIT {

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserServiceProperties userServiceProperties;

    @MockitoBean
    private RestTemplate restTemplate;

    @Value("${telegram.bot.secret-token}")
    private String token;

    private TelegramUpdate telegramUpdate;

    private String jwtToken;

    @BeforeEach
    public void setup() {
        telegramUpdate = TestUtil.createTelegramUpdate();
        jwtToken = jwtTestUtil.generateToken(1L);
    }

    @Test
    public void receiveUpdate_successfullyLinkTelegram() {
        telegramUpdate.getMessage().setText("/start " + jwtToken);
        telegramUpdate.getMessage().getChat().setId(1L);
        ArgumentCaptor<HttpEntity<TelegramChatLinkRequest>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Telegram-Bot-Api-Secret-Token", token);
        HttpEntity<TelegramUpdate> request = TestUtil.createRequest(telegramUpdate, headers);

        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/v1/telegram/webhook",
                HttpMethod.POST,
                request,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String url = String.format("http://%s:%s/api/v1/users",
                userServiceProperties.getHost(), userServiceProperties.getPort()) + "/telegram";
        verify(restTemplate, times(1)).exchange(
                eq(url),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(Void.class)
        );

        HttpEntity<TelegramChatLinkRequest> linkRequest = captor.getValue();
        TelegramChatLinkRequest linkBody = linkRequest.getBody();
        assertNotNull(linkBody);
        assertEquals(1L, linkBody.getUserId());
        assertEquals(1L, linkBody.getChatId());

        HttpHeaders linkHeaders = linkRequest.getHeaders();
        assertEquals(userServiceProperties.getApiKey(), linkHeaders.getFirst("X-Api-Key"));
    }

    @Test
    public void receiveUpdate_ifSomeExc_returnOk() {
        telegramUpdate.getMessage().setText("/start " + jwtToken);
        telegramUpdate.getMessage().getChat().setId(1L);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Telegram-Bot-Api-Secret-Token", token);
        HttpEntity<TelegramUpdate> request = TestUtil.createRequest(telegramUpdate, headers);

        String url = String.format("http://%s:%s/api/v1/users",
                userServiceProperties.getHost(), userServiceProperties.getPort()) + "/telegram";
        var httpStatusCodeException = new HttpStatusCodeException(HttpStatus.UNAUTHORIZED) {};
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(httpStatusCodeException);

        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/v1/telegram/webhook",
                HttpMethod.POST,
                request,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void receiveUpdate_invalidTelegramToken_returnUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Telegram-Bot-Api-Secret-Token", "invalid-token");
        HttpEntity<TelegramUpdate> request = TestUtil.createRequest(telegramUpdate, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange(
                "/api/v1/telegram/webhook",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Token: invalid-token invalid", error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }
}
