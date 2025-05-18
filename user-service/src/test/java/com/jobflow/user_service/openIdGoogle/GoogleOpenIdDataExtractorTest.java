package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdUserInfo;
import com.jobflow.user_service.user.AuthProvider;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoogleOpenIdDataExtractorTest {

    private static final String CLAIM = "claim";

    @Mock
    private JWTClaimsSet jwtClaimsSet;

    @InjectMocks
    private GoogleOpenIdDataExtractor openIdDataExtractor;

    @Test
    public void extractData_returnData() throws ParseException {
        when(jwtClaimsSet.getClaimAsString(CLAIM)).thenReturn("data");

        String result = openIdDataExtractor.extractData(jwtClaimsSet, CLAIM);

        assertNotNull(result);
        assertEquals("data", result);

        verify(jwtClaimsSet, times(1)).getClaimAsString(CLAIM);
    }

    @Test
    public void extractData_failedParse_throwExc() throws ParseException {
        var parseException = new ParseException("Parse exception", 0);
        when(jwtClaimsSet.getClaimAsString(CLAIM)).thenThrow(parseException);

        var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> openIdDataExtractor.extractData(jwtClaimsSet, CLAIM));
        assertEquals("Failed to extract claim: " + CLAIM + " from JWT: " + parseException.getMessage(), openIdServiceException.getMessage());
    }

    @Test
    public void extractUserInfo_returnUserInfo() throws ParseException {
        when(jwtClaimsSet.getClaimAsString("given_name")).thenReturn("Ivan");
        when(jwtClaimsSet.getClaimAsString("family_name")).thenReturn("Ivanov");
        when(jwtClaimsSet.getClaimAsString("sub")).thenReturn("123");
        when(jwtClaimsSet.getClaimAsString("picture")).thenReturn("test-url");

        OpenIdUserInfo result = openIdDataExtractor.extractUserInfo(jwtClaimsSet);

        assertNotNull(result);
        assertEquals("Ivan", result.getFirstname());
        assertEquals("Ivanov", result.getLastname());
        assertEquals(AuthProvider.GOOGLE, result.getAuthProvider());
        assertEquals("123", result.getAuthProviderId());
        assertEquals("test-url", result.getAvatarUrl());
    }
}