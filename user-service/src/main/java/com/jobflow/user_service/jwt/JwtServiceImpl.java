package com.jobflow.user_service.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    @Override
    public Claims extractClaims(String token) {
        return null;
    }

    @Override
    public String extractLogin(String token) {
        return "";
    }
}
