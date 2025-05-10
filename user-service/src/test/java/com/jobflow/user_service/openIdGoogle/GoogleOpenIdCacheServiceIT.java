package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.BaseIT;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleOpenIdCacheServiceIT extends BaseIT {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private GoogleOpenIdCacheService openIdCacheService;

    @Test
    public void getJwkSet_returnJwkSetAndCacheCorrectly() {
        String firstCall = redisTemplate.opsForValue().get("openid:google::jwkset");
        assertNull(firstCall);

        JWKSet result = openIdCacheService.getJwkSet();
        assertNotNull(result);

        String secondCall = redisTemplate.opsForValue().get("openid:google::jwkset");
        assertNotNull(secondCall);
    }
}
