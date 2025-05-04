package com.jobflow.user_service.openId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.IdTokenValidationException;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.exception.StateValidationException;
import com.jobflow.user_service.handler.GlobalHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OpenIdControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OpenIdService openIdService;

    private OpenIdController openIdController;

    private MockMvc mockMvc;

    private OpenIdRequest openIdRequest;

    private String  openIdRequestJson;

    private OpenIdResponse openIdResponse;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        when(openIdService.getProviderName()).thenReturn(TestUtil.PROVIDER);
        openIdController = new OpenIdController(List.of(openIdService));

        mockMvc = MockMvcBuilders.standaloneSetup(openIdController)
                .setControllerAdvice(new GlobalHandler())
                .build();

        openIdRequest = new OpenIdRequest(TestUtil.PROVIDER, TestUtil.STATE, TestUtil.AUTH_CODE);
        openIdRequestJson = objectMapper.writeValueAsString(openIdRequest);
        openIdResponse = new OpenIdResponse(TestUtil.ACCESS_TOKEN, TestUtil.REFRESH_TOKEN);

    }

    @Test
    public void getJwtTokens_returnOpenIdResponse() throws Exception {
        when(openIdService.getJwtTokens(openIdRequest)).thenReturn(openIdResponse);

        mockMvc.perform(post("/api/v1/openid")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(openIdRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(openIdResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(openIdResponse.getRefreshToken()));

        verify(openIdService, times(1)).getJwtTokens(openIdRequest);
    }

    @Test
    public void getJwtTokens_invalidData_returnBadRequest() throws Exception {
        OpenIdRequest invalidRequest = new OpenIdRequest(null, "", "");
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/openid")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verify(openIdService, never()).getJwtTokens(any());
    }

    @Test
    public void getJwtTokens_unsupportedProvider_returnBadRequest() throws Exception {
        openIdRequest.setProvider(OpenIdProvider.GITHUB); //In this test, mock supports only "GOOGLE"
        openIdRequestJson = objectMapper.writeValueAsString(openIdRequest);

        mockMvc.perform(post("/api/v1/openid")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(openIdRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported provider: " + openIdRequest.getProvider()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verify(openIdService, never()).getJwtTokens(any());
    }

    @Test
    public void getJwtTokens_invalidState_returnBadRequest() throws Exception {
        var stateValidationException = new StateValidationException("Invalid state");
        when(openIdService.getJwtTokens(openIdRequest)).thenThrow(stateValidationException);

        mockMvc.perform(post("/api/v1/openid")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(openIdRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(stateValidationException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verify(openIdService, times(1)).getJwtTokens(openIdRequest);
    }

    @Test
    public void getJwtTokens_invalidIdToken_returnBadRequest() throws Exception {
        var idTokenValidationException = new IdTokenValidationException("Invalid id token");
        when(openIdService.getJwtTokens(openIdRequest)).thenThrow(idTokenValidationException);

        mockMvc.perform(post("/api/v1/openid")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(openIdRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(idTokenValidationException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verify(openIdService, times(1)).getJwtTokens(openIdRequest);
    }

    @Test
    public void getJwtTokens_openIdServiceException_returnInternalServerError() throws Exception {
        var openIdServiceException = new OpenIdServiceException("OpenID service exception");
        when(openIdService.getJwtTokens(openIdRequest)).thenThrow(openIdServiceException);

        mockMvc.perform(post("/api/v1/openid")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(openIdRequestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(openIdServiceException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        verify(openIdService, times(1)).getJwtTokens(openIdRequest);
    }
}