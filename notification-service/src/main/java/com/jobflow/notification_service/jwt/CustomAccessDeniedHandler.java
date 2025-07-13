package com.jobflow.notification_service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.notification_service.handler.ResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (authentication != null) ? authentication.getName() : "Anonymous";

        LOGGER.error("[Authorization Error]: {} for request: {} by userId: {}",
                accessDeniedException.getMessage(), request.getRequestURI(), userId);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ResponseError responseError = ResponseError.buildResponseError(
                accessDeniedException.getMessage(), HttpServletResponse.SC_FORBIDDEN
        );
        String responseErrorJson = objectMapper.writeValueAsString(responseError);

        response.getWriter().write(responseErrorJson);

    }
}

