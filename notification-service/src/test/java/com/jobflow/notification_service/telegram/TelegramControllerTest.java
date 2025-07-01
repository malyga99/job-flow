package com.jobflow.notification_service.telegram;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.exception.InvalidTelegramTokenException;
import com.jobflow.notification_service.exception.UserClientException;
import com.jobflow.notification_service.handler.GlobalHandler;
import org.junit.experimental.results.ResultMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TelegramControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private TelegramController telegramController;

    private MockMvc mockMvc;

    private TelegramUpdate telegramUpdate;

    private String telegramUpdateJson;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(telegramController)
                .setControllerAdvice(new GlobalHandler())
                .build();

        telegramUpdate = TestUtil.createTelegramUpdate();
        telegramUpdateJson = objectMapper.writeValueAsString(telegramUpdate);
    }

    @Test
    public void receiveUpdate_returnOk() throws Exception {
        doNothing().when(telegramService).processUpdate(telegramUpdate, "test-token");

        mockMvc.perform(post("/api/v1/telegram/webhook")
                        .header("X-Telegram-Bot-Api-Secret-Token", "test-token")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(telegramUpdateJson))
                .andExpect(status().isOk());

        verify(telegramService, times(1)).processUpdate(telegramUpdate, "test-token");
    }

    @Test
    public void receiveUpdate_invalidToken_returnUnauthorized() throws Exception {
        var invalidTokenExc = new InvalidTelegramTokenException("Invalid token");
        doThrow(invalidTokenExc).when(telegramService).processUpdate(telegramUpdate, "test-token");

        mockMvc.perform(post("/api/v1/telegram/webhook")
                        .header("X-Telegram-Bot-Api-Secret-Token", "test-token")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(telegramUpdateJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(invalidTokenExc.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));

        verify(telegramService, times(1)).processUpdate(telegramUpdate, "test-token");
    }
}