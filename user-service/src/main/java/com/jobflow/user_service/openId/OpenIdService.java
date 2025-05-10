package com.jobflow.user_service.openId;

import com.jobflow.user_service.user.AuthProvider;

public interface OpenIdService {

    OpenIdResponse getJwtTokens(OpenIdRequest openIdRequest);

    String exchangeAuthCode(String authCode);

    AuthProvider getProviderName();
}
