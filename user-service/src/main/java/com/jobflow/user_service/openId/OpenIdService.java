package com.jobflow.user_service.openId;

public interface OpenIdService {

    OpenIdResponse getJwtTokens(OpenIdRequest openIdRequest);

    String exchangeAuthCode(String authCode);

    OpenIdProvider getProviderName();
}
