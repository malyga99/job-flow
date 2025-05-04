package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.IdTokenValidationException;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdJwtUtils;
import com.jobflow.user_service.openId.OpenIdTokenValidator;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class GoogleOpenIdTokenValidator implements OpenIdTokenValidator {

    private final GoogleOpenIdProperties openIdProperties;
    private final GoogleOpenIdCacheService cacheService;

    @Override
    public void validateIdToken(SignedJWT idToken) {
        JWTClaimsSet claims = OpenIdJwtUtils.extractClaims(idToken);

        String issuer = claims.getIssuer();
        String audience = claims.getAudience().get(0);
        Date expirationTime = claims.getExpirationTime();

        validateIssuer(issuer);
        validateAudience(audience);
        validateExpirationTime(expirationTime);
        validateJWKSet(idToken);
    }

    private void validateIssuer(String issuer) {
        if (!issuer.equals("https://accounts.google.com")) {
            throw new IdTokenValidationException("Invalid issuer: " + issuer);
        }
    }


    private void validateAudience(String audience) {
        if (!audience.equals(openIdProperties.getClientId())) {
            throw new IdTokenValidationException("Invalid audience: " + audience);
        }
    }

    private void validateExpirationTime(Date expirationTime) {
        if (expirationTime.toInstant().isBefore(Instant.now())) {
            throw new IdTokenValidationException("Id token has expired");
        }
    }

    private void validateJWKSet(SignedJWT idToken) {
        boolean isValid = false;
        String keyId = idToken.getHeader().getKeyID();

        JWKSet jwkSet = cacheService.getJwkSet();
        for (JWK jwk : jwkSet.getKeys()) {
            if (jwk.getKeyID().equals(keyId)) {
                try {
                    RSASSAVerifier rsassaVerifier = new RSASSAVerifier(jwk.toRSAKey());
                    isValid = idToken.verify(rsassaVerifier);
                } catch (JOSEException e) {
                    throw new OpenIdServiceException("JWK validate exception: " + e.getMessage(), e);
                }
            }
        }

        if (!isValid) {
            throw new IdTokenValidationException("Id token not valid");
        }
    }
}
