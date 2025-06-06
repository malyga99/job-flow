package com.jobflow.user_service.auth;

import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.web.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@Tag(
        name = "Authentication controller",
        description = "Handles user authentication"
)
public class AuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user and returns access/refresh JWT tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = AuthenticationResponse.class))),

                    @ApiResponse(responseCode = "404", description = "User with this login not found",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "401", description = "Authentication exception, e.g Bad credentials",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @PostMapping
    public ResponseEntity<AuthenticationResponse> auth(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Authentication details", required = true
            ) AuthenticationRequest authenticationRequest,
            HttpServletRequest request
    ) {
        LOGGER.info("[POST] Authenticate request received for login: {}", authenticationRequest.getLogin());

        String ip = IpUtils.extractClientIp(request);
        return ResponseEntity.ok(authenticationService.auth(authenticationRequest, ip));
    }

    @Operation(
            summary = "Logout user",
            description = "Logs out and revokes the refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logout successful",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = Void.class))),

                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "401", description = "Authentication exception",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Logout details", required = true
            ) LogoutRequest logoutRequest
    ) {
        LOGGER.info("[POST] Logout request received");

        authenticationService.logout(logoutRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Refresh token",
            description = "Refreshes token and returns a new access token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refresh successful",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = String.class))),

                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "401", description = "Authentication exception, e.g revoked token",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token details", required = true
            ) RefreshTokenRequest refreshTokenRequest
    ) {
        LOGGER.info("[POST] Refresh token request received");

        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
    }
}
