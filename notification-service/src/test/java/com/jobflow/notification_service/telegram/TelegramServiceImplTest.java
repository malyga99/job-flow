package com.jobflow.notification_service.telegram;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.exception.InvalidTelegramTokenException;
import com.jobflow.notification_service.exception.UserClientException;
import com.jobflow.notification_service.jwt.JwtService;
import com.jobflow.notification_service.user.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TelegramServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserClient userClient;

    private static final String VALID_TOKEN = "test-token";

    private TelegramServiceImpl telegramService;

    private TelegramUpdate telegramUpdate;

    @BeforeEach
    public void setup() {
        telegramService = new TelegramServiceImpl(jwtService, userClient, VALID_TOKEN);

        telegramUpdate = TestUtil.createTelegramUpdate();
    }

    @Test
    public void processUpdate_processUpdateSuccessfully() {
        telegramUpdate.getMessage().setText("/start some-token");
        telegramUpdate.getMessage().getChat().setId(1L);
        when(jwtService.extractUserId("some-token")).thenReturn("1");

        telegramService.processUpdate(telegramUpdate, VALID_TOKEN);

        verify(userClient, times(1)).linkChatId(1L, 1L);
    }

    @Test
    public void processUpdate_messageIsNull_skipUpdate() {
        telegramUpdate.setMessage(null);

        telegramService.processUpdate(telegramUpdate, VALID_TOKEN);

        verifyNoInteractions(jwtService, userClient);
    }

    @Test
    public void processUpdate_messageTestIsNull_skipUpdate() {
        telegramUpdate.getMessage().setText(null);

        telegramService.processUpdate(telegramUpdate, VALID_TOKEN);

        verifyNoInteractions(jwtService, userClient);
    }

    @Test
    public void processUpdate_textDoesNotStartsWithStart_skipUpdate() {
        telegramUpdate.getMessage().setText("some-token");

        telegramService.processUpdate(telegramUpdate, VALID_TOKEN);

        verifyNoInteractions(jwtService, userClient);
    }

    @Test
    public void processUpdate_invalidToken_throwExc() {
        var invalidTelegramTokenException = assertThrows(InvalidTelegramTokenException.class,
                () -> telegramService.processUpdate(telegramUpdate, "invalid-token"));

        assertEquals("Token: invalid-token invalid", invalidTelegramTokenException.getMessage());
    }

    @Test
    public void processUpdate_ifExc_doesNotThrowAnything() {
        telegramUpdate.getMessage().setText("/start some-token");
        telegramUpdate.getMessage().getChat().setId(1L);
        when(jwtService.extractUserId("some-token")).thenReturn("1");

        var userClientException = new UserClientException("User client exception");
        doThrow(userClientException).when(userClient).linkChatId(1L, 1L);

        assertDoesNotThrow(() -> telegramService.processUpdate(telegramUpdate, VALID_TOKEN));
    }

}