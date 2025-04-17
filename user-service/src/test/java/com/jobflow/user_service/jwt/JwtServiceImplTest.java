package com.jobflow.user_service.jwt;

import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    private UserDetails userDetails;

    private final String SECRET_KEY = "d1V6OXhGSnR2TFlFN01mdVhnaHFrZTBSakh6QWRCWVE";

    @BeforeEach
    public void setup() {
        userDetails = new User(1L, "Ivan", "Ivanov", "IvanIvanov@gmail.com", "abcde", Role.ROLE_USER);
        jwtService = new JwtServiceImpl(
                SECRET_KEY,
                300000L,
                604800000L
        );;
    }

    @Test
    public void generateAccessToken_returnGeneratedToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtService.extractClaims(token);
        assertEquals(userDetails.getUsername(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getId());
    }

    @Test
    public void generateAccessToken_withClaims_returnGeneratedTokenWithClaims() {
        String token = jwtService.generateAccessToken(Map.of("first-claim", "first-value", "second-claim", "second-value"),userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtService.extractClaims(token);
        assertEquals(userDetails.getUsername(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getId());
        assertEquals("first-value", claims.get("first-claim"));
        assertEquals("second-value", claims.get("second-claim"));
    }

    @Test
    public void generateRefreshToken_returnGeneratedToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtService.extractClaims(token);
        assertEquals(userDetails.getUsername(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getId());
    }

    @Test
    public void generateRefreshToken_withClaims_returnGeneratedTokenWithClaims() {
        String token = jwtService.generateRefreshToken(Map.of("first-claim", "first-value", "second-claim", "second-value"),userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtService.extractClaims(token);
        assertEquals(userDetails.getUsername(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getId());
        assertEquals("first-value", claims.get("first-claim"));
        assertEquals("second-value", claims.get("second-claim"));
    }

    @Test
    public void extractClaims_returnExtractedClaims() {
        String token = jwtService.generateAccessToken(userDetails);
        Claims claims = jwtService.extractClaims(token);

        assertNotNull(claims);
        assertEquals(userDetails.getUsername(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getId());
    }

    @Test
    public void extractLogin_returnExtractedLogin() {
        String token = jwtService.generateAccessToken(userDetails);
        String login = jwtService.extractLogin(token);

        assertNotNull(login);
        assertEquals(userDetails.getUsername(), login);
    }

    @Test
    public void extractExpiration_returnExtractedExpiration() {
        String token = jwtService.generateAccessToken(userDetails);
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(new Date().before(expiration));
    }

    @Test
    public void isValid_returnTrue() {
        String token = jwtService.generateAccessToken(userDetails);
        boolean result = jwtService.isValid(userDetails, token);

        assertTrue(result);
    }

    @Test
    public void isValid_userNotMatches_returnFalse() {
        String token = jwtService.generateAccessToken(userDetails);
        boolean result = jwtService.isValid(User.builder().login("NotIvanIvanov@gmail.com").build(), token);

        assertFalse(result);
    }


}