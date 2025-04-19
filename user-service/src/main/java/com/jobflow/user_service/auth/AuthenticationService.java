package com.jobflow.user_service.auth;

public interface AuthenticationService {

    AuthenticationResponse auth(AuthenticationRequest authenticationRequest);
}
