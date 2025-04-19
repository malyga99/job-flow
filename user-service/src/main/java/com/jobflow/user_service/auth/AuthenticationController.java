package com.jobflow.user_service.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @PostMapping
    public ResponseEntity<AuthenticationResponse> auth(
            @RequestBody @Valid AuthenticationRequest authenticationRequest
    ) {
        LOGGER.info("[POST] Authenticate request received for login: {}", authenticationRequest.getLogin());
        return ResponseEntity.ok(authenticationService.auth(authenticationRequest));
    }
}
