package com.jobflow.user_service.register;

import com.jobflow.user_service.handler.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/register")
@RequiredArgsConstructor
@Tag(
        name = "Registration controller",
        description = "Handles user registration"
)
public class RegisterController {

    private final RegisterService registerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

    @Operation(
            summary = "User registration",
            description = "Processes user registration and sends a confirmation code to the provided email address. User account will be activated after successful code verification",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Code sent successfully"),

                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "409", description = "User with this login already exists",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @PostMapping
    public ResponseEntity<Void> register(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration details", required = true
            ) RegisterRequest registerRequest
    ) {
        LOGGER.info("[POST] Register request received for login: {}", registerRequest.getLogin());
        registerService.register(registerRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Resend code",
            description = "Resends a new verification code to the provided email address",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Code resent successfully",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = RegisterResponse.class))),

                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "410", description = "Code expired",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),
            }
    )
    @PostMapping("/resend")
    public ResponseEntity<Void> resendCode(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Resend code details", required = true
            ) ResendCodeRequest resendCodeRequest
    ) {
        LOGGER.info("[POST] Resend code request received for login: {}", resendCodeRequest.getLogin());
        registerService.resendCode(resendCodeRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Code confirmation",
            description = "Confirms the verification code which sent to the email address, registers a user and returns access/refresh JWT tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Code confirmed successfully",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = RegisterResponse.class))),

                    @ApiResponse(responseCode = "400", description = "Validation error or invalid verification code",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "410", description = "Code expired",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),
            }
    )
    @PostMapping("/confirm")
    public ResponseEntity<RegisterResponse> confirmCode(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Confirm code details", required = true
            ) ConfirmCodeRequest confirmCodeRequest
    ) {
        LOGGER.info("[POST] Confirm code request received for login: {}", confirmCodeRequest.getLogin());
        return ResponseEntity.ok(registerService.confirmCode(confirmCodeRequest));
    }
}
