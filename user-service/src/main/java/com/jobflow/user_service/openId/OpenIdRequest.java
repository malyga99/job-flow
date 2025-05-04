package com.jobflow.user_service.openId;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OpenID request")
public class OpenIdRequest {

    @NotNull(message = "Provider must be filled in")
    @Schema(description = "OpenID provider", example = "GOOGLE")
    private OpenIdProvider provider;

    @NotBlank(message = "State must be filled in")
    @Schema(description = "State", example = "state")
    private String state;

    @NotBlank(message = "Authorization code must be filled in")
    @Schema(description = "Authorization code", example = "authCode")
    private String authCode;
}
