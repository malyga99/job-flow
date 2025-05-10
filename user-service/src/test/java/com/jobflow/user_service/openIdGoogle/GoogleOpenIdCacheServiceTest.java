package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoogleOpenIdCacheServiceTest {

    @Mock
    private JWKSet jwkSet;

    @InjectMocks
    private GoogleOpenIdCacheService openIdCacheService;

    private URL url;

    @BeforeEach
    public void setup() throws MalformedURLException {
        url = new URL("https://www.googleapis.com/oauth2/v3/certs");
    }

    @Test
    public void getJwkSet_returnJwkSet() {
        try (MockedStatic<JWKSet> mockedStatic = mockStatic(JWKSet.class)) {
            mockedStatic.when(() -> JWKSet.load(url)).thenReturn(jwkSet);

            JWKSet result = openIdCacheService.getJwkSet();

            assertNotNull(result);
            assertEquals(jwkSet, result);
            assertSame(jwkSet, result);

            mockedStatic.verify(() -> JWKSet.load(url), times(1));
        }
    }

    @Test
    public void getJwkSet_failedLoad_throwExc() {
        try (MockedStatic<JWKSet> mockedStatic = mockStatic(JWKSet.class)) {
            var ioException = new IOException("Load exception");
            mockedStatic.when(() -> JWKSet.load(url)).thenThrow(ioException);

            var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> openIdCacheService.getJwkSet());
            assertEquals("Jwk set load exception: " + ioException.getMessage(), openIdServiceException.getMessage());

            mockedStatic.verify(() -> JWKSet.load(url), times(1));
        }
    }

    @Test
    public void getJwkSet_failedParse_throwExc() {
        try (MockedStatic<JWKSet> mockedStatic = mockStatic(JWKSet.class)) {
            var parseException = new ParseException("Parse exception", 0);
            mockedStatic.when(() -> JWKSet.load(url)).thenThrow(parseException);

            var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> openIdCacheService.getJwkSet());
            assertEquals("Jwk set load exception: " + parseException.getMessage(), openIdServiceException.getMessage());

            mockedStatic.verify(() -> JWKSet.load(url), times(1));
        }
    }

}