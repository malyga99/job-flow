package com.jobflow.user_service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.UserNotFoundException;
import com.jobflow.user_service.handler.GlobalHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;

    private AuthenticationRequest authenticationRequest;

    private LogoutRequest logoutRequest;

    private AuthenticationResponse authenticationResponse;

    private String authenticationRequestJson;

    private String logoutRequestJson;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalHandler())
                .build();
        authenticationRequest = new AuthenticationRequest("IvanIvanov@gmail.com", "abcde");
        authenticationResponse = new AuthenticationResponse("access.jwt.token", "refresh.jwt.token");
        authenticationRequestJson = objectMapper.writeValueAsString(authenticationRequest);
        logoutRequest = new LogoutRequest("refresh.jwt.token");
        logoutRequestJson = objectMapper.writeValueAsString(logoutRequest);
    }

    @Test
    public void auth_returnAuthResponse() throws Exception {
        when(authenticationService.auth(authenticationRequest)).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(authenticationRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(authenticationResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(authenticationResponse.getRefreshToken()));

        verify(authenticationService, times(1)).auth(authenticationRequest);
    }

    @Test
    public void auth_invalidData_returnBadRequest() throws Exception {
        AuthenticationRequest invalidRequest = new AuthenticationRequest("", "");
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.time").exists());

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void auth_userNotFound_returnNotFound() throws Exception {
        var userNotFoundException = new UserNotFoundException("User with login: " + authenticationRequest.getLogin() + " not found");
        when(authenticationService.auth(authenticationRequest)).thenThrow(userNotFoundException);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(authenticationRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(userNotFoundException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.time").exists());

        verify(authenticationService, times(1)).auth(authenticationRequest);
    }

    @Test
    public void logout_successfullyRevokeToken() throws Exception {
        doNothing().when(authenticationService).logout(logoutRequest);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(logoutRequestJson))
                .andExpect(status().isOk());

        verify(authenticationService, times(1)).logout(logoutRequest);
    }

}