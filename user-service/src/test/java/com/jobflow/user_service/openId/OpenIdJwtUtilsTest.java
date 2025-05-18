package com.jobflow.user_service.openId;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenIdJwtUtilsTest {

    @Test
    public void getJwt_returnValidJwt() {
        String validJwt = createValidJwt();

        SignedJWT jwt = OpenIdJwtUtils.getJwt(validJwt);

        assertNotNull(jwt);
    }

    @Test
    public void getJwt_parseFailed_throwExc() {
        String invalidJwt = "invalid-jwt";

        var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> OpenIdJwtUtils.getJwt(invalidJwt));
        assertTrue(openIdServiceException.getMessage().contains("Failed to parse JWT: "));
    }

    @Test
    public void extractClaims_returnClaims() throws Exception {
        SignedJWT jwt = SignedJWT.parse(createValidJwt());

        JWTClaimsSet claims = OpenIdJwtUtils.extractClaims(jwt);

        assertEquals("test-subject", claims.getSubject());
    }

    @Test
    public void extractClaims_parseFailed_throwExc() throws Exception {
        var parseException = new ParseException("Parse exc", 0);
        SignedJWT mockJwt = mock(SignedJWT.class);
        when(mockJwt.getJWTClaimsSet()).thenThrow(parseException);

        var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> OpenIdJwtUtils.extractClaims(mockJwt));
        assertEquals("Failed to extract claims from JWT: " + parseException.getMessage(), openIdServiceException.getMessage());
    }

    private String createValidJwt() {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject("test-subject")
                    .issuer("https://issuer")
                    .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                    .build();

            String secret = "super-secret-key-that-is-at-least-32-bytes";
            JWSSigner signer = new MACSigner(secret.getBytes());

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            jwt.sign(signer);
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}