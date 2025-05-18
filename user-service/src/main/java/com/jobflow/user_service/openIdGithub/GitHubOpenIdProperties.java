package com.jobflow.user_service.openIdGithub;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openid.github")
@Getter
@Setter
public class GitHubOpenIdProperties {

    private String state;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
