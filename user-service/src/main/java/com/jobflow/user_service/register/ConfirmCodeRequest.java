package com.jobflow.user_service.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Confirm code request")
public class ConfirmCodeRequest {

    @NotBlank(message = "Login must be filled in")
    @Size(max = 100, message = "Maximum length of a login is 100 characters")
    @Schema(description = "User login", example = "IvanIvanov@gmail.com")
    private String login;

    @NotNull(message = "Code must be filled in")
    @Min(value = 100000L, message = "Code must be six digits")
    @Max(value = 999999L, message = "Code must be six digits")
    private int code;
}
