package com.jobflow.user_service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.handler.ResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = (authentication != null) ? authentication.getName() : "Anonymous";

        LOGGER.error("[Authentication Error]: {} for request: {} by user: {}", authException.getMessage(), request.getRequestURI(), login);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ResponseError responseError = ResponseError.buildResponseError(authException.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
        String responseErrorJson = objectMapper.writeValueAsString(responseError);

        response.getWriter().write(responseErrorJson);
    }
}
