package com.jobflow.user_service.openId;

import com.nimbusds.jwt.SignedJWT;

public interface OpenIdTokenValidator {

    void validateIdToken(SignedJWT idToken);
}
