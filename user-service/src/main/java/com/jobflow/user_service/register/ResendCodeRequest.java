package com.jobflow.user_service.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resend code request")
public class ResendCodeRequest {

    @NotBlank(message = "Login must be filled in")
    @Size(max = 100, message = "Maximum length of a login is 100 characters")
    @Schema(description = "User login", example = "IvanIvanov@gmail.com")
    private String login;
}
