package com.jobflow.user_service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.TokenRevokedException;
import com.jobflow.user_service.exception.TooManyRequestsException;
import com.jobflow.user_service.exception.UserNotFoundException;
import com.jobflow.user_service.handler.GlobalHandler;
import jakarta.servlet.http.HttpServletRequest;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;

    private AuthenticationRequest authenticationRequest;

    private LogoutRequest logoutRequest;

    private RefreshTokenRequest refreshTokenRequest;

    private AuthenticationResponse authenticationResponse;

    private String authenticationRequestJson;

    private String logoutRequestJson;

    private String refreshTokenRequestJson;


    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalHandler())
                .build();
        authenticationRequest = TestUtil.createAuthRequest();
        authenticationRequestJson = objectMapper.writeValueAsString(authenticationRequest);
        authenticationResponse = new AuthenticationResponse(TestUtil.ACCESS_TOKEN, TestUtil.REFRESH_TOKEN);

        logoutRequest = TestUtil.createLogoutRequest();
        logoutRequestJson = objectMapper.writeValueAsString(logoutRequest);

        refreshTokenRequest = TestUtil.createRefreshRequest();
        refreshTokenRequestJson = objectMapper.writeValueAsString(refreshTokenRequest);
    }

    @Test
    public void auth_returnAuthResponse() throws Exception {
        when(authenticationService.auth(eq(authenticationRequest), any(String.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(authenticationRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(authenticationResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(authenticationResponse.getRefreshToken()));

        verify(authenticationService, times(1)).auth(eq(authenticationRequest), any(String.class));
    }

    @Test
    public void auth_invalidData_returnBadRequest() throws Exception {
        AuthenticationRequest invalidRequest = new AuthenticationRequest("", "");
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void auth_userNotFound_returnNotFound() throws Exception {
        var userNotFoundException = new UserNotFoundException("User with login: " + authenticationRequest.getLogin() + " not found");
        when(authenticationService.auth(eq(authenticationRequest), any(String.class))).thenThrow(userNotFoundException);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(authenticationRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(userNotFoundException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        verify(authenticationService, times(1)).auth(eq(authenticationRequest), any(String.class));
    }

    @Test
    public void auth_tooManyRequests_returnTooManyRequests() throws Exception {
        var tooManyRequestException = new TooManyRequestsException("Too many login attempts. Try again in a minute");
        when(authenticationService.auth(eq(authenticationRequest), any(String.class))).thenThrow(tooManyRequestException);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(authenticationRequestJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(tooManyRequestException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.value()));

        verify(authenticationService, times(1)).auth(eq(authenticationRequest), any(String.class));
    }

    @Test
    public void logout_successfullyRevokeToken() throws Exception {
        doNothing().when(authenticationService).logout(logoutRequest);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(logoutRequestJson))
                .andExpect(status().isOk());

        verify(authenticationService, times(1)).logout(logoutRequest);
    }

    @Test
    public void logout_invalidData_returnBadRequest() throws Exception {
        LogoutRequest invalidRequest = new LogoutRequest("");
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void logout_tooManyRequests_returnTooManyRequests() throws Exception {
        var tooManyRequestException = new TooManyRequestsException("Too many logout attempts. Try again in a minute");
        doThrow(tooManyRequestException).when(authenticationService).logout(logoutRequest);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(logoutRequestJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(tooManyRequestException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.value()));

        verify(authenticationService, times(1)).logout(logoutRequest);
    }

    @Test
    public void refresh_successfullyRefreshToken() throws Exception {
        when(authenticationService.refreshToken(refreshTokenRequest)).thenReturn(TestUtil.ACCESS_TOKEN);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(refreshTokenRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(TestUtil.ACCESS_TOKEN));

        verify(authenticationService, times(1)).refreshToken(refreshTokenRequest);
    }

    @Test
    public void refresh_invalidData_returnBadRequest() throws Exception {
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("");
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void refresh_revokedToken_returnUnauthorized() throws Exception {
        var tokenRevokedException = new TokenRevokedException("Token with id: some-id revoked");
        when(authenticationService.refreshToken(refreshTokenRequest)).thenThrow(tokenRevokedException);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(refreshTokenRequestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(tokenRevokedException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));

        verify(authenticationService, times(1)).refreshToken(refreshTokenRequest);
    }

    @Test
    public void refresh_tooManyRequests_returnTooManyRequests() throws Exception {
        var tooManyRequestException = new TooManyRequestsException("Too many token refresh attempts. Try again in a minute");
        when(authenticationService.refreshToken(refreshTokenRequest)).thenThrow(tooManyRequestException);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(refreshTokenRequestJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(tooManyRequestException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.value()));

        verify(authenticationService, times(1)).refreshToken(refreshTokenRequest);
    }
}