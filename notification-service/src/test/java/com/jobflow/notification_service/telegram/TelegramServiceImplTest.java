package com.jobflow.notification_service.telegram;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.exception.InvalidTelegramTokenException;
import com.jobflow.notification_service.exception.UserClientException;
import com.jobflow.notification_service.jwt.JwtService;
import com.jobflow.notification_service.user.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TelegramServiceImplTest {

    private static final String VALID_SECRET_TOKEN = "secret-token";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserClient userClient;

    @Mock
    private TelegramProperties telegramProperties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TelegramServiceImpl telegramService;

    private TelegramUpdate telegramUpdate;

    @BeforeEach
    public void setup() {
        telegramUpdate = TestUtil.createTelegramUpdate();
    }

    @Test
    public void processUpdate_processUpdateSuccessfully() {
        telegramUpdate.getMessage().setText("/start some-token");
        telegramUpdate.getMessage().getChat().setId(1L);
        when(telegramProperties.getBotSecretToken()).thenReturn(VALID_SECRET_TOKEN);
        when(jwtService.extractUserId("some-token")).thenReturn("1");

        telegramService.processUpdate(telegramUpdate, VALID_SECRET_TOKEN);

        verify(userClient, times(1)).linkChatId(1L, 1L);
    }

    @Test
    public void processUpdate_messageIsNull_skipUpdate() {
        telegramUpdate.setMessage(null);
        when(telegramProperties.getBotSecretToken()).thenReturn(VALID_SECRET_TOKEN);

        telegramService.processUpdate(telegramUpdate, VALID_SECRET_TOKEN);

        verifyNoInteractions(jwtService, userClient);
    }

    @Test
    public void processUpdate_messageTestIsNull_skipUpdate() {
        telegramUpdate.getMessage().setText(null);
        when(telegramProperties.getBotSecretToken()).thenReturn(VALID_SECRET_TOKEN);

        telegramService.processUpdate(telegramUpdate, VALID_SECRET_TOKEN);

        verifyNoInteractions(jwtService, userClient);
    }

    @Test
    public void processUpdate_textDoesNotStartsWithStart_skipUpdate() {
        telegramUpdate.getMessage().setText("some-token");
        when(telegramProperties.getBotSecretToken()).thenReturn(VALID_SECRET_TOKEN);

        telegramService.processUpdate(telegramUpdate, VALID_SECRET_TOKEN);

        verifyNoInteractions(jwtService, userClient);
    }

    @Test
    public void processUpdate_invalidToken_throwExc() {
        when(telegramProperties.getBotSecretToken()).thenReturn(VALID_SECRET_TOKEN);
        var invalidTelegramTokenException = assertThrows(InvalidTelegramTokenException.class,
                () -> telegramService.processUpdate(telegramUpdate, "invalid-token"));

        assertEquals("Token: invalid-token invalid", invalidTelegramTokenException.getMessage());
    }

    @Test
    public void processUpdate_ifExc_doesNotThrowAnything() {
        telegramUpdate.getMessage().setText("/start some-token");
        telegramUpdate.getMessage().getChat().setId(1L);
        when(telegramProperties.getBotSecretToken()).thenReturn(VALID_SECRET_TOKEN);
        when(jwtService.extractUserId("some-token")).thenReturn("1");

        var userClientException = new UserClientException("User client exception");
        doThrow(userClientException).when(userClient).linkChatId(1L, 1L);

        assertDoesNotThrow(() -> telegramService.processUpdate(telegramUpdate, VALID_SECRET_TOKEN));
    }

    @Test
    public void send_sendMessageSuccessfully() {
        ArgumentCaptor<HttpEntity<Map<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        when(telegramProperties.getBotToken()).thenReturn("bot-token");

        telegramService.send(1L, "some-message");

        verify(restTemplate, times(1)).exchange(
                eq(String.format("https://api.telegram.org/bot%s/sendMessage", "bot-token")),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(Void.class)
        );

        HttpEntity<Map<String, String>> request = captor.getValue();
        Map<String, String> body = request.getBody();
        assertNotNull(body);
        assertEquals("1", body.get("chat_id"));
        assertEquals("some-message", body.get("text"));
    }

}