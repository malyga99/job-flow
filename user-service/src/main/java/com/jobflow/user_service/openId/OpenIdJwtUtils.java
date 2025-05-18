package com.jobflow.user_service.openId;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;

public final class OpenIdJwtUtils {

    private OpenIdJwtUtils() {

    }

    public static SignedJWT getJwt(String idToken) {
        try {
            return SignedJWT.parse(idToken);
        } catch (ParseException e) {
            throw new OpenIdServiceException("Failed to parse JWT: " + e.getMessage(), e);
        }
    }

    public static JWTClaimsSet extractClaims(SignedJWT signedJWT) {
        try {
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new OpenIdServiceException("Failed to extract claims from JWT: " + e.getMessage(), e);
        }
    }
}
