package com.jobflow.user_service.user;

import com.jobflow.user_service.handler.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Operation(
            summary = "Get user info",
            description = "Retrieves user info by ID. It is used for micro-service interaction",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User info received successfully",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = UserInfoDto.class))),

                    @ApiResponse(responseCode = "404", description = "User with this ID not found",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "401", description = "Authentication exception, e.g incorrect api key",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @GetMapping("/info")
    public ResponseEntity<UserInfoDto> getUserInfo(
            @RequestParam("userId") @Parameter(description = "User ID", example = "1", required = true) Long userId,
            @RequestHeader("X-API-Key") @Parameter(in = ParameterIn.HEADER,
                    description = "Api key", example = "api-key", required = true) String apiKey
    ) {
        LOGGER.info("[GET] Request for get user info by id: {}", userId);

        return ResponseEntity.ok(userService.getUserInfo(userId, apiKey));
    }

    @Operation(
            summary = "Link telegram",
            description = "Linking user with telegram chat. It is used for micro-service interaction",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Telegram linked successfully",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = Void.class))),

                    @ApiResponse(responseCode = "404", description = "User with this ID not found",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "401", description = "Authentication exception, e.g incorrect api key",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @PostMapping("/telegram")
    public ResponseEntity<Void> linkTelegram(
            @RequestHeader("X-API-Key") @Parameter(in = ParameterIn.HEADER,
                    description = "Api key", example = "api-key", required = true) String apiKey,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Telegram chat link details", required = true
            ) TelegramChatLinkRequest linkRequest
    ) {
        LOGGER.info("[POST] Linking telegram request received. chatId: {}, userId: {}",
                linkRequest.getChatId(), linkRequest.getUserId());
        userService.linkTelegram(linkRequest, apiKey);

        return ResponseEntity.ok().build();
    }
}
