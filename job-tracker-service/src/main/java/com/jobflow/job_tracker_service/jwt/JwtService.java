package com.jobflow.job_tracker_service.jwt;

import io.jsonwebtoken.Claims;

import java.security.Key;

public interface JwtService {

    Claims extractClaims(String token);

    String extractUserId(String token);

    Key getSecretKey();
}

