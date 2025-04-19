package com.jobflow.user_service.auth;

import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.jwt.JwtServiceImpl;
import com.jobflow.user_service.user.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
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
}
