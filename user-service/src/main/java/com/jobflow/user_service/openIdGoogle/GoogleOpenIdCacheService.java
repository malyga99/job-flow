package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdCacheService;
import com.nimbusds.jose.jwk.JWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

@Service
@CacheConfig(cacheNames = "openid:google")
public class GoogleOpenIdCacheService implements OpenIdCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOpenIdCacheService.class);

    @Override
    @Cacheable(key = "'jwkset'")
    public JWKSet getJwkSet() {
        LOGGER.debug("Attempting to load JWK set from Google endpoint");
        try {
            JWKSet jwkSet = JWKSet.load(new URL("https://www.googleapis.com/oauth2/v3/certs"));
            LOGGER.debug("Successfully loaded JWK set from Google endpoint");

            return jwkSet;
        } catch (IOException | ParseException e) {
            throw new OpenIdServiceException("Jwk set load exception: " + e.getMessage(), e);
        }
    }
}
