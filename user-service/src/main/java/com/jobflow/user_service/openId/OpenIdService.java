package com.jobflow.user_service.openId;

import com.jobflow.user_service.user.AuthProvider;

public interface OpenIdService {

    OpenIdResponse getJwtTokens(OpenIdRequest openIdRequest, String clientIp);

    String exchangeAuthCode(String authCode);

    AuthProvider getProviderName();

    String extractToken(String response);
}
