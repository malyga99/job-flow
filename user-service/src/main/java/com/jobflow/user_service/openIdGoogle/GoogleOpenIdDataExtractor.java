package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdDataExtractor;
import com.jobflow.user_service.openId.OpenIdUserInfo;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class GoogleOpenIdDataExtractor implements OpenIdDataExtractor<JWTClaimsSet> {

    @Override
    public String extractData(JWTClaimsSet data, String key) {
        try {
            return data.getClaimAsString(key);
        } catch (ParseException e) {
            throw new OpenIdServiceException("Failed to extract claim: " + key + " from JWT: " + e.getMessage(), e);
        }
    }

    @Override
    public OpenIdUserInfo extractUserInfo(JWTClaimsSet data) {
        return OpenIdUserInfo.builder()
                .firstname(extractData(data, "given_name"))
                .lastname(extractData(data, "family_name"))
                .login(extractData(data, "email"))
                .avatarUrl(extractData(data, "picture"))
                .build();
    }

}
