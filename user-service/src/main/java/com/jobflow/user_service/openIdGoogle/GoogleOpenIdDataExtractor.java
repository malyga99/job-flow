package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdDataExtractor;
import com.jobflow.user_service.openId.OpenIdUserInfo;
import com.jobflow.user_service.user.AuthProvider;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class GoogleOpenIdDataExtractor implements OpenIdDataExtractor<JWTClaimsSet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOpenIdDataExtractor.class);

    @Override
    public String extractData(JWTClaimsSet data, String key) {
        LOGGER.debug("Extracting claim: {} from JWT", key);
        try {
            String claim = data.getClaimAsString(key);
            LOGGER.debug("Successfully extracted claim: {} from JWT", key);

            return claim;
        } catch (ParseException e) {
            throw new OpenIdServiceException("Failed to extract claim: " + key + " from JWT: " + e.getMessage(), e);
        }
    }

    @Override
    public OpenIdUserInfo extractUserInfo(JWTClaimsSet data) {
        LOGGER.debug("Extracting user info from JWT");
        OpenIdUserInfo userInfo = OpenIdUserInfo.builder()
                .firstname(extractData(data, "given_name"))
                .lastname(extractData(data, "family_name"))
                .authProvider(AuthProvider.GOOGLE)
                .authProviderId(extractData(data, "sub"))
                .avatarUrl(extractData(data, "picture"))
                .build();
        LOGGER.debug("Successfully extracted user info from JWT. Provider id: {}", userInfo.getAuthProviderId());

        return userInfo;
    }

}
