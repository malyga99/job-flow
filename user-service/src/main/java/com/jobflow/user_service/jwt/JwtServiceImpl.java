package com.jobflow.user_service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtServiceImpl.class);

    private final String SECRET_KEY;
    private final long ACCESS_EXPIRATION_TIME;
    private final long REFRESH_EXPIRATION_TIME;

    public JwtServiceImpl(@Value("${jwt.secret-key}") String SECRET_KEY,
                          @Value("${jwt.access-expiration-time}") long ACCESS_EXPIRATION_TIME,
                          @Value("${jwt.refresh-expiration-time}") long REFRESH_EXPIRATION_TIME) {
        this.SECRET_KEY = SECRET_KEY;
        this.ACCESS_EXPIRATION_TIME = ACCESS_EXPIRATION_TIME;
        this.REFRESH_EXPIRATION_TIME = REFRESH_EXPIRATION_TIME;
    }

    @Override
    public String generateAccessToken(Map<String, Object> claims, UserDetails user) {
        LOGGER.debug("Generating access token for userId: {}", user.getUsername());
        String tokenId = UUID.randomUUID().toString();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME))
                .setId(tokenId)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(Map<String, Object> claims, UserDetails user) {
        LOGGER.debug("Generating refresh token for userId: {}", user.getUsername());
        String tokenId = UUID.randomUUID().toString();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .setId(tokenId)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateAccessToken(UserDetails user) {
        return generateAccessToken(new HashMap<>(), user);
    }

    @Override
    public String generateRefreshToken(UserDetails user) {
        return generateRefreshToken(new HashMap<>(), user);
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
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    @Override
    public boolean isValid(UserDetails user, String token) {
        boolean matchesUserId = extractUserId(token).equals(user.getUsername());

        LOGGER.debug("Validated token by userId: {}. matchesUserId: {}", user.getUsername(), matchesUserId);
        return matchesUserId;
    }

    @Override
    public Key getSecretKey() {
        byte[] decodeKey = Decoders.BASE64.decode(SECRET_KEY);

        return Keys.hmacShaKeyFor(decodeKey);
    }
}
