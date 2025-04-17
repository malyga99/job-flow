package com.jobflow.user_service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.handler.ResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authenticationException;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    private SecurityContext securityContext;

    @Test
    public void commence_returnAuthenticationException() throws ServletException, IOException {
        String errorMessage = "Invalid token";
        String expectedJson = "{\"message\": \"Invalid token\", \"status\": 401, \"time\": 2025-01-01 01:01}";
        ArgumentCaptor<ResponseError> argumentCaptor = ArgumentCaptor.forClass(ResponseError.class);

        when(authenticationException.getMessage()).thenReturn(errorMessage);
        when(objectMapper.writeValueAsString(any(ResponseError.class))).thenReturn(expectedJson);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationEntryPoint.commence(request, response, authenticationException);

        verify(objectMapper, times(1)).writeValueAsString(argumentCaptor.capture());
        verify(printWriter, times(1)).write(expectedJson);
        verify(response, times(1)).setContentType("application/json");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ResponseError responseError = argumentCaptor.getValue();
        assertNotNull(responseError);
        assertEquals(errorMessage, responseError.getMessage());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, responseError.getStatus());
        assertNotNull(responseError.getTime());
    }

}