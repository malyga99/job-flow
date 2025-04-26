package com.jobflow.user_service.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String firstname;

    private String lastname;

    private String login;

    private String password;
}
