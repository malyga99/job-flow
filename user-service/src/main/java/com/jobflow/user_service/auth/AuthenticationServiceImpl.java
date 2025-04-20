package com.jobflow.user_service.auth;

import com.jobflow.user_service.exception.TokenRevokedException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;
    private static final String BLACKLIST_KEY = "blacklist:refresh:%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Override
    public AuthenticationResponse auth(AuthenticationRequest authenticationRequest) {
        LOGGER.debug("Starting user authentication with login: {}", authenticationRequest.getLogin());
        authenticationRequest.setLogin(authenticationRequest.getLogin().toLowerCase());

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getLogin(),
                authenticationRequest.getPassword()
        ));

        User user = (User) authenticate.getPrincipal();
        LOGGER.debug("Successfully user authentication with login: {}", authenticationRequest.getLogin());

        return new AuthenticationResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        User currentUser = userService.getCurrentUser();
        LOGGER.debug("Starting logout process for user: {}", currentUser.getLogin());

        String refreshToken = logoutRequest.getRefreshToken();
        Claims claims = jwtService.extractClaims(refreshToken);

        Date expiration = claims.getExpiration();
        String tokenId = claims.getId();

        long ttl = calculateTtl(expiration);
        if (ttl > 0) {
            String key = String.format(BLACKLIST_KEY, tokenId);
            redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.SECONDS);
            LOGGER.debug("Successfully revoked refresh token with jti: {} (TTL: {} seconds) for user: {}", tokenId, ttl, currentUser.getLogin());
        } else {
            LOGGER.debug("Refresh token with jti: {} for user: {} already expired", tokenId, currentUser.getLogin());
        }
    }

    @Override
    public String refreshToken(RefreshTokenRequest refreshTokenRequest) {
        User currentUser = userService.getCurrentUser();

        String refreshToken = refreshTokenRequest.getRefreshToken();
        Claims claims = jwtService.extractClaims(refreshToken);

        String tokenId = claims.getId();
        validateIsTokenRevoked(tokenId);

        return jwtService.generateAccessToken(currentUser);
    }

    private void validateIsTokenRevoked(String tokenId) {
        String key = String.format(BLACKLIST_KEY, tokenId);
        Boolean isRevoked = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(isRevoked)) {
            throw new TokenRevokedException("Token with id: " + tokenId + " revoked");
        }
    }

    private long calculateTtl(Date expiration) {
        return Duration.between(Instant.now(), expiration.toInstant())
                .toSeconds();
    }
}
