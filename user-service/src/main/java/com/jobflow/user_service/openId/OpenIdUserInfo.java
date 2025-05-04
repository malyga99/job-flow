package com.jobflow.user_service.openId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A dto, containing user information extracted from an external OpenID provider (Google, GitHub, etc.).
 * Is used to match or create a new user in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenIdUserInfo {

    private String firstname;

    private String lastname;

    private String login;

    private String avatarUrl;
}
