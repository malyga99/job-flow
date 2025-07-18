package com.jobflow.notification_service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * A utility class for generating a JWT token using
 * a secret from test/application.properties
 */
@Component
public class JwtTestUtil {
    @Value("${jwt.secret-key}")
    private String secretKey;

    public String generateToken(Long userId) {
        byte[] decodeKey = Decoders.BASE64.decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(decodeKey);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
