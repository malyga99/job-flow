package com.jobflow.notification_service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private static final String SECRET_KEY = "d1V6OXhGSnR2TFlFN01mdVhnaHFrZTBSakh6QWRCWVE";
    private static final String USER_ID = "1";

    private JwtServiceImpl jwtService;

    @BeforeEach
    public void setup() {
        jwtService = new JwtServiceImpl(SECRET_KEY);
    }

    @Test
    public void extractClaims_returnExtractedClaims() {
        String token = generateToken(USER_ID);
        Claims claims = jwtService.extractClaims(token);

        assertNotNull(claims);
        assertEquals(USER_ID, claims.getSubject());
    }

    @Test
    public void extractUserId_returnExtractedUserId() {
        String token = generateToken(USER_ID);
        String userId = jwtService.extractUserId(token);

        assertNotNull(userId);
        assertEquals(USER_ID, userId);
    }

    @Test
    public void getSecretKey_returnSecretKey() {
        Key key = jwtService.getSecretKey();

        assertNotNull(key);
    }

    private String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .signWith(SignatureAlgorithm.HS256,  jwtService.getSecretKey())
                .compact();
    }
}