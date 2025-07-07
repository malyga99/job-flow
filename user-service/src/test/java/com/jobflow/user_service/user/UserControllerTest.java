package com.jobflow.user_service.user;

import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.InvalidApiKeyException;
import com.jobflow.user_service.exception.UserNotFoundException;
import com.jobflow.user_service.handler.GlobalHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private UserInfoDto userInfo;

    private TelegramChatLinkRequest linkRequest;

    private String linkRequestJson;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalHandler())
                .build();

        userInfo = TestUtil.createUserInfo();

        linkRequest = TestUtil.createLinkRequest();
        linkRequestJson = objectMapper.writeValueAsString(linkRequest);
    }

    @Test
    public void getUserInfo_returnUserInfo() throws Exception {
        when(userService.getUserInfo(1L, "test-key")).thenReturn(userInfo);

        mockMvc.perform(get("/api/v1/users/info")
                        .param("userId", "1")
                        .header("X-API-Key", "test-key")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userInfo.getEmail()))
                .andExpect(jsonPath("$.telegramChatId").value(userInfo.getTelegramChatId()));

        verify(userService, times(1)).getUserInfo(1L, "test-key");
    }

    @Test
    public void getUserInfo_invalidApiKey_returnUnauthorized() throws Exception {
        var invalidApiKeyExc = new InvalidApiKeyException("Invalid api key");
        when(userService.getUserInfo(1L, "test-key")).thenThrow(invalidApiKeyExc);

        mockMvc.perform(get("/api/v1/users/info")
                        .param("userId", "1")
                        .header("X-API-Key", "test-key")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(invalidApiKeyExc.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));

        verify(userService, times(1)).getUserInfo(1L, "test-key");
    }

    @Test
    public void getUserInfo_userNotFound_returnNotFound() throws Exception {
        var userNotFoundException = new UserNotFoundException("User not found");
        when(userService.getUserInfo(1L, "test-key")).thenThrow(userNotFoundException);

        mockMvc.perform(get("/api/v1/users/info")
                        .param("userId", "1")
                        .header("X-API-Key", "test-key")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(userNotFoundException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        verify(userService, times(1)).getUserInfo(1L, "test-key");
    }

    @Test
    public void linkTelegram_returnOk() throws Exception {
        doNothing().when(userService).linkTelegram(linkRequest, "test-key");

        mockMvc.perform(post("/api/v1/users/telegram")
                        .header("X-API-Key", "test-key")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(linkRequestJson))
                .andExpect(status().isOk());

        verify(userService, times(1)).linkTelegram(linkRequest, "test-key");
    }

    @Test
    public void linkTelegram_invalidApiKey_returnUnauthorized() throws Exception {
        var invalidApiKeyExc = new InvalidApiKeyException("Invalid api key");
        doThrow(invalidApiKeyExc).when(userService).linkTelegram(linkRequest, "test-key");

        mockMvc.perform(post("/api/v1/users/telegram")
                        .header("X-API-Key", "test-key")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(linkRequestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(invalidApiKeyExc.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));

        verify(userService, times(1)).linkTelegram(linkRequest, "test-key");
    }

    @Test
    public void linkTelegram_userNotFound_returnNotFound() throws Exception {
        var userNotFoundException = new UserNotFoundException("User not found");
        doThrow(userNotFoundException).when(userService).linkTelegram(linkRequest, "test-key");

        mockMvc.perform(post("/api/v1/users/telegram")
                        .header("X-API-Key", "test-key")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(linkRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(userNotFoundException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        verify(userService, times(1)).linkTelegram(linkRequest, "test-key");
    }
}