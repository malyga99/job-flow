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
@Schema(description = "Register request")
public class RegisterRequest {

    @NotBlank(message = "Firstname must be filled in")
    @Size(max = 100, message = "Maximum length of a firstname is 100 characters")
    @Schema(description = "User firstname", example = "Ivan")
    private String firstname;

    @NotBlank(message = "Lastname must be filled in")
    @Size(max = 100, message = "Maximum length of a lastname is 100 characters")
    @Schema(description = "User lastname", example = "Ivanov")
    private String lastname;

    @NotBlank(message = "Login must be filled in")
    @Size(max = 100, message = "Maximum length of a login is 100 characters")
    @Schema(description = "User login", example = "IvanIvanov@gmail.com")
    private String login;

    @NotBlank(message = "Password must be filled in")
    @Size(min = 5, max = 100, message = "Password length must be from 5 to 100 characters")
    @Schema(description = "User password", example = "abcde")
    private String password;
}
