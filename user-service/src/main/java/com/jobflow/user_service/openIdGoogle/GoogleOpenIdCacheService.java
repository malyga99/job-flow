package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdCacheService;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

@Service
@CacheConfig(cacheNames = "openid")
public class GoogleOpenIdCacheService implements OpenIdCacheService {

    @Override
    @Cacheable(key = "'jwkset'")
    public JWKSet getJwkSet() {
        try {
            JWKSet jwkSet = JWKSet.load(new URL("https://www.googleapis.com/oauth2/v3/certs"));
            return jwkSet;
        } catch (IOException | ParseException e) {
            throw new OpenIdServiceException("Jwk set load exception: " + e.getMessage(), e);
        }
    }
}
