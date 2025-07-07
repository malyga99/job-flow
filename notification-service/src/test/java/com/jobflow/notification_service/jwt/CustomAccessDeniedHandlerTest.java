package com.jobflow.notification_service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.notification_service.handler.ResponseError;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AccessDeniedException accessDeniedException;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private CustomAccessDeniedHandler accessDeniedHandler;

    private String errorMessage;

    private String expectedJson;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();

        errorMessage = "Access denied";
        expectedJson = "{\"message\": \"Access denied\", \"status\": 403, \"time\": 2025-01-01 01:01}";
    }

    @Test
    public void handle_returnAuthorizationException() throws IOException, ServletException {
        ArgumentCaptor<ResponseError> argumentCaptor = ArgumentCaptor.forClass(ResponseError.class);

        when(accessDeniedException.getMessage()).thenReturn(errorMessage);
        when(objectMapper.writeValueAsString(any(ResponseError.class))).thenReturn(expectedJson);
        when(response.getWriter()).thenReturn(printWriter);

        accessDeniedHandler.handle(request, response, accessDeniedException);

        verify(objectMapper, times(1)).writeValueAsString(argumentCaptor.capture());
        verify(printWriter, times(1)).write(expectedJson);
        verify(response, times(1)).setContentType("application/json");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);

        ResponseError responseError = argumentCaptor.getValue();
        assertNotNull(responseError);
        assertEquals(errorMessage, responseError.getMessage());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseError.getStatus());
        assertNotNull(responseError.getTime());
    }

    @Test
    public void handle_withAuthenticatedUser_returnAuthorizationException() throws IOException, ServletException {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);
        when(context.getAuthentication()).thenReturn(authentication);

        when(accessDeniedException.getMessage()).thenReturn(errorMessage);
        when(objectMapper.writeValueAsString(any(ResponseError.class))).thenReturn(expectedJson);
        when(response.getWriter()).thenReturn(printWriter);

        accessDeniedHandler.handle(request, response, accessDeniedException);

        verify(objectMapper).writeValueAsString(any(ResponseError.class));
        verify(printWriter).write(expectedJson);
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

}