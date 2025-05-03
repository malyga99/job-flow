package com.jobflow.user_service;

import com.jobflow.user_service.auth.AuthenticationRequest;
import com.jobflow.user_service.auth.LogoutRequest;
import com.jobflow.user_service.auth.RefreshTokenRequest;
import com.jobflow.user_service.register.ConfirmCodeRequest;
import com.jobflow.user_service.register.RegisterRequest;
import com.jobflow.user_service.register.ResendCodeRequest;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public final class TestUtil {

    public static final String LOGIN = "ivanivanov@gmail.com";
    public static final String PASSWORD = "abcde";
    public static final String REFRESH_TOKEN = "refresh.jwt.token";
    public static final String ACCESS_TOKEN = "access.jwt.token";
    public static final int CODE = 111111;

    private TestUtil() {
    }

    public static HttpEntity<MultiValueMap<String, Object>> createMultipartRequest(Map<String, Object> parts, HttpHeaders headers) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        parts.forEach(body::add);

        return new HttpEntity<>(body, headers);
    }

    public static HttpEntity<MultiValueMap<String, Object>> createMultipartRequest(Map<String, Object> parts) {
        return createMultipartRequest(parts, null);
    }


    public static <T> HttpEntity<T> createRequest(T request, HttpHeaders headers) {
        return new HttpEntity<>(request, headers);
    }

    public static <T> HttpEntity<T> createRequest(T request) {
        return createRequest(request, null);
    }

    public static AuthenticationRequest createAuthRequest() {
        return new AuthenticationRequest(LOGIN, PASSWORD);
    }

    public static RegisterRequest createRegisterRequest() {
        return new RegisterRequest("Ivan", "Ivanov", LOGIN, PASSWORD, null);
    }

    public static LogoutRequest createLogoutRequest() {
        return new LogoutRequest(REFRESH_TOKEN);
    }

    public static RefreshTokenRequest createRefreshRequest() {
        return new RefreshTokenRequest(REFRESH_TOKEN);
    }

    public static ConfirmCodeRequest createConfirmCodeRequest() {
        return new ConfirmCodeRequest(LOGIN, CODE);
    }

    public static ResendCodeRequest createResendCodeRequest() {
        return new ResendCodeRequest(LOGIN);
    }

    public static User createUser() {
        return User.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login(LOGIN)
                .password(PASSWORD)
                .avatar("dummy".getBytes())
                .role(Role.ROLE_USER)
                .build();
    }
}
