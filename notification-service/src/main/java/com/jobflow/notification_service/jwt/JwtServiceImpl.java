package com.jobflow.notification_service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtServiceImpl.class);

    private final String SECRET_KEY;

    public JwtServiceImpl(@Value("${jwt.secret-key}") String SECRET_KEY) {
        this.SECRET_KEY = SECRET_KEY;
    }

    @Override
    public Claims extractClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        LOGGER.debug("Extracted claims from token for userId: {}", claims.getSubject());
        return claims;
    }

    @Override
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public Key getSecretKey() {
        byte[] decodeKey = Decoders.BASE64.decode(SECRET_KEY);

        return Keys.hmacShaKeyFor(decodeKey);
    }
}
