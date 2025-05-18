package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.IdTokenValidationException;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdCacheService;
import com.jobflow.user_service.openId.OpenIdJwtUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOpenIdTokenValidatorTest {

    private static final String VALID_ISSUER = "https://accounts.google.com";
    private static final String VALID_AUDIENCE = "test-audience";
    private static final String VALID_KEY_ID = "test-key-id";

    @Mock
    private GoogleOpenIdProperties openIdProperties;

    @Mock
    private OpenIdCacheService openIdCacheService;

    @Mock
    private SignedJWT idToken;

    @Mock
    private JWTClaimsSet claims;

    @Mock
    private JWSHeader jwsHeader;

    @Mock
    private JWKSet jwkSet;

    @Mock
    private JWK jwk;

    @Mock
    private RSAKey rsaKey;

    @Mock
    private RSAPublicKey rsaPublicKey;

    @InjectMocks
    private GoogleOpenIdTokenValidator googleOpenIdTokenValidator;

    @Test
    public void validateIdToken_validIdToken_doNothing() throws JOSEException {
        try (MockedStatic<OpenIdJwtUtils> mockedStatic = mockStatic(OpenIdJwtUtils.class)) {
            mockedStatic.when(() -> OpenIdJwtUtils.extractClaims(idToken)).thenReturn(claims);
            when(claims.getIssuer()).thenReturn(VALID_ISSUER);
            when(claims.getAudience()).thenReturn(List.of(VALID_AUDIENCE));
            when(claims.getExpirationTime()).thenReturn(Date.from(Instant.now().plusSeconds(3600L)));
            when(openIdProperties.getClientId()).thenReturn(VALID_AUDIENCE);

            when(idToken.getHeader()).thenReturn(jwsHeader);
            when(jwsHeader.getKeyID()).thenReturn(VALID_KEY_ID);

            when(openIdCacheService.getJwkSet()).thenReturn(jwkSet);
            when(jwkSet.getKeys()).thenReturn(List.of(jwk));

            when(jwk.getKeyID()).thenReturn(VALID_KEY_ID);
            when(jwk.toRSAKey()).thenReturn(rsaKey);
            when(rsaKey.toRSAPublicKey()).thenReturn(rsaPublicKey);
            when(idToken.verify(any(RSASSAVerifier.class))).thenReturn(true);

            assertDoesNotThrow(() -> googleOpenIdTokenValidator.validateIdToken(idToken));
            verify(idToken, times(1)).verify(any(RSASSAVerifier.class));
            verify(openIdCacheService, times(1)).getJwkSet();
        }
    }

    @Test
    public void validateIdToken_invalidIssuer_throwExc() {
        try (MockedStatic<OpenIdJwtUtils> mockedStatic = mockStatic(OpenIdJwtUtils.class)) {
            String invalidIssuer = "invalidIssuer";
            mockedStatic.when(() -> OpenIdJwtUtils.extractClaims(idToken)).thenReturn(claims);
            when(claims.getIssuer()).thenReturn(invalidIssuer);
            when(claims.getAudience()).thenReturn(List.of(VALID_AUDIENCE));

            var idTokenValidationException = assertThrows(IdTokenValidationException.class, () -> googleOpenIdTokenValidator.validateIdToken(idToken));
            assertEquals("Invalid issuer: " + invalidIssuer, idTokenValidationException.getMessage());
        }
    }

    @Test
    public void validateIdToken_invalidAudience_throwExc() {
        try (MockedStatic<OpenIdJwtUtils> mockedStatic = mockStatic(OpenIdJwtUtils.class)) {
            String invalidAudience = "invalidAudience";
            mockedStatic.when(() -> OpenIdJwtUtils.extractClaims(idToken)).thenReturn(claims);
            when(claims.getIssuer()).thenReturn(VALID_ISSUER);
            when(openIdProperties.getClientId()).thenReturn(VALID_AUDIENCE);
            when(claims.getAudience()).thenReturn(List.of(invalidAudience));

            var idTokenValidationException = assertThrows(IdTokenValidationException.class, () -> googleOpenIdTokenValidator.validateIdToken(idToken));
            assertEquals("Invalid audience: " + invalidAudience, idTokenValidationException.getMessage());
        }
    }

    @Test
    public void validateIdToken_expiredToken_throwExc() {
        try (MockedStatic<OpenIdJwtUtils> mockedStatic = mockStatic(OpenIdJwtUtils.class)) {
            mockedStatic.when(() -> OpenIdJwtUtils.extractClaims(idToken)).thenReturn(claims);
            when(claims.getIssuer()).thenReturn(VALID_ISSUER);
            when(openIdProperties.getClientId()).thenReturn(VALID_AUDIENCE);
            when(claims.getAudience()).thenReturn(List.of(VALID_AUDIENCE));
            when(claims.getExpirationTime()).thenReturn(Date.from(Instant.now().minusSeconds(3600L)));

            var idTokenValidationException = assertThrows(IdTokenValidationException.class, () -> googleOpenIdTokenValidator.validateIdToken(idToken));
            assertEquals("Id token has expired", idTokenValidationException.getMessage());
        }
    }

    @Test
    public void validateIdToken_invalidIdToken_throwExc() throws JOSEException {
        try (MockedStatic<OpenIdJwtUtils> mockedStatic = mockStatic(OpenIdJwtUtils.class)) {
            mockedStatic.when(() -> OpenIdJwtUtils.extractClaims(idToken)).thenReturn(claims);
            when(claims.getIssuer()).thenReturn(VALID_ISSUER);
            when(claims.getAudience()).thenReturn(List.of(VALID_AUDIENCE));
            when(claims.getExpirationTime()).thenReturn(Date.from(Instant.now().plusSeconds(3600L)));
            when(openIdProperties.getClientId()).thenReturn(VALID_AUDIENCE);

            when(idToken.getHeader()).thenReturn(jwsHeader);
            when(jwsHeader.getKeyID()).thenReturn("invalidKeyId");

            when(openIdCacheService.getJwkSet()).thenReturn(jwkSet);
            when(jwkSet.getKeys()).thenReturn(List.of(jwk));

            when(jwk.getKeyID()).thenReturn(VALID_KEY_ID);

            var idTokenValidationException = assertThrows(IdTokenValidationException.class, () -> googleOpenIdTokenValidator.validateIdToken(idToken));
            assertEquals("Id token not valid", idTokenValidationException.getMessage());
            verify(idToken, never()).verify(any(RSASSAVerifier.class));
            verify(openIdCacheService, times(1)).getJwkSet();
        }
    }

    @Test
    public void validateIdToken_verifyFailed_throwExc() throws JOSEException {
        try (MockedStatic<OpenIdJwtUtils> mockedStatic = mockStatic(OpenIdJwtUtils.class)) {
            mockedStatic.when(() -> OpenIdJwtUtils.extractClaims(idToken)).thenReturn(claims);
            when(claims.getIssuer()).thenReturn(VALID_ISSUER);
            when(claims.getAudience()).thenReturn(List.of(VALID_AUDIENCE));
            when(claims.getExpirationTime()).thenReturn(Date.from(Instant.now().plusSeconds(3600L)));
            when(openIdProperties.getClientId()).thenReturn(VALID_AUDIENCE);

            when(idToken.getHeader()).thenReturn(jwsHeader);
            when(jwsHeader.getKeyID()).thenReturn(VALID_KEY_ID);

            when(openIdCacheService.getJwkSet()).thenReturn(jwkSet);
            when(jwkSet.getKeys()).thenReturn(List.of(jwk));

            when(jwk.getKeyID()).thenReturn(VALID_KEY_ID);
            when(jwk.toRSAKey()).thenReturn(rsaKey);
            when(rsaKey.toRSAPublicKey()).thenReturn(rsaPublicKey);

            var joseException = new JOSEException("Verify exception");
            when(idToken.verify(any(RSASSAVerifier.class))).thenThrow(joseException);

            var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> googleOpenIdTokenValidator.validateIdToken(idToken));
            assertEquals("JWK validate exception: " + joseException.getMessage(), openIdServiceException.getMessage());

            verify(idToken, times(1)).verify(any(RSASSAVerifier.class));
            verify(openIdCacheService, times(1)).getJwkSet();
        }
    }
}