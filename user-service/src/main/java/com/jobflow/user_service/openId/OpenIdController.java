package com.jobflow.user_service.openId;

import com.jobflow.user_service.exception.UnsupportedProviderException;
import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.user.AuthProvider;
import com.jobflow.user_service.web.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@RestController
@RequestMapping("/api/v1/openid")
@Tag(
        name = "OpenID controller",
        description = "Handles OpenID authentication and token exchange"
)
public class OpenIdController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdController.class);

    private final Map<AuthProvider, OpenIdService> openIdServices;

    public OpenIdController(List<OpenIdService> openIdServicesList) {
        openIdServices = openIdServicesList.stream()
                .collect(toMap(OpenIdService::getProviderName, Function.identity()));
    }

    @Operation(
            summary = "Exchange authorization code for JWT tokens",
            description = "Receives OpenID provider, authorization code and state, and returns access/refresh JWT tokens after successful validation and token exchange",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved JWT tokens",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = OpenIdResponse.class))),

                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),


                    @ApiResponse(responseCode = "400", description = "Validation error, unsupported provider or invalid state/ID token",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @PostMapping
    public ResponseEntity<OpenIdResponse> getJwtTokens(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "OpenID request details", required = true
            ) OpenIdRequest openIdRequest,
            HttpServletRequest request
    ) {
        AuthProvider provider = openIdRequest.getProvider();
        LOGGER.info("[POST] OpenID request received for provider: {}", provider);

        OpenIdService openIdService = openIdServices.get(provider);
        if (openIdService == null) {
            throw new UnsupportedProviderException("Unsupported provider: " + provider);
        }

        String ip = IpUtils.extractClientIp(request);
        return ResponseEntity.ok(openIdService.getJwtTokens(openIdRequest, ip));
    }
}
