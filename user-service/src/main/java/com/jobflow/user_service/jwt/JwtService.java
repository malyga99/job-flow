package com.jobflow.user_service.jwt;

import io.jsonwebtoken.Claims;

public interface JwtService {

    Claims extractClaims(String token);

    String extractLogin(String token);
}
