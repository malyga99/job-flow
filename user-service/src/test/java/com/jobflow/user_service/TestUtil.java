package com.jobflow.user_service;

import com.jobflow.user_service.auth.AuthenticationRequest;
import com.jobflow.user_service.auth.LogoutRequest;
import com.jobflow.user_service.auth.RefreshTokenRequest;
import com.jobflow.user_service.openId.OpenIdUserInfo;
import com.jobflow.user_service.user.*;
import com.jobflow.user_service.openId.OpenIdRequest;
import com.jobflow.user_service.register.ConfirmCodeRequest;
import com.jobflow.user_service.register.RegisterRequest;
import com.jobflow.user_service.register.ResendCodeRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public final class TestUtil {

    public static final String USER_ID = "1";
    public static final String FIRST_NAME = "Ivan";
    public static final String LAST_NAME = "Ivanov";
    public static final String LOGIN = "ivanivanov@gmail.com";
    public static final String PASSWORD = "abcde";

    public static final String REFRESH_TOKEN = "refresh.jwt.token";
    public static final String ACCESS_TOKEN = "access.jwt.token";
    public static final int CODE = 111111;

    public static final AuthProvider AUTH_PROVIDER = AuthProvider.GOOGLE;
    public static final String AUTH_PROVIDER_ID = "123";

    public static final String STATE = "state";
    public static final String AUTH_CODE = "authCode";
    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_SECRET = "client-secret";
    public static final String REDIRECT_URI = "redirect-uri";

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

    public static <T> HttpEntity<T> createRequest(HttpHeaders headers) {
        return createRequest(null, headers);
    }

    public static AuthenticationRequest createAuthRequest() {
        return new AuthenticationRequest(LOGIN, PASSWORD);
    }

    public static RegisterRequest createRegisterRequest() {
        return new RegisterRequest(FIRST_NAME, LAST_NAME, LOGIN, PASSWORD, null);
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

    public static OpenIdRequest createOpenIdRequest() {
        return new OpenIdRequest(AUTH_PROVIDER, STATE, AUTH_CODE);
    }

    public static OpenIdUserInfo createOpenIdUserInfo() {
        return OpenIdUserInfo.builder()
                .firstname(FIRST_NAME)
                .lastname(LAST_NAME)
                .authProvider(AUTH_PROVIDER)
                .authProviderId(AUTH_PROVIDER_ID)
                .build();
    }

    public static User createUser() {
        return User.builder()
                .id(Long.valueOf(USER_ID))
                .firstname(FIRST_NAME)
                .lastname(LAST_NAME)
                .login(LOGIN)
                .password(PASSWORD)
                .role(Role.ROLE_USER)
                .authProvider(AuthProvider.LOCAL)
                .telegramChatId(1L)
                .build();
    }

    public static UserInfoDto createUserInfo() {
        return UserInfoDto.builder()
                .email(LOGIN)
                .telegramChatId(1L)
                .build();
    }

    public static TelegramChatLinkRequest createLinkRequest() {
        return TelegramChatLinkRequest.builder()
                .userId(Long.valueOf(USER_ID))
                .chatId(1L)
                .build();
    }
}
