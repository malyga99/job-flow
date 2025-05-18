package com.jobflow.user_service.openId;

import com.nimbusds.jose.jwk.JWKSet;

public interface OpenIdCacheService {

    JWKSet getJwkSet();
}
