package com.jobflow.user_service.auth;

public interface AuthenticationService {

    AuthenticationResponse auth(AuthenticationRequest authenticationRequest, String clientIp);

    void logout(LogoutRequest logoutRequest);

    String refreshToken(RefreshTokenRequest refreshTokenRequest);
}
