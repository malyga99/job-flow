package com.jobflow.user_service.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;

public interface JwtService {

    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    String generateAccessToken(Map<String, Object> claims, UserDetails user);

    String generateRefreshToken(Map<String, Object> claims, UserDetails user);

    Claims extractClaims(String token);

    String extractLogin(String token);

    Date extractExpiration(String token);

    boolean isValid(UserDetails user, String token);
}
