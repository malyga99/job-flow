package com.jobflow.notification_service.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "user.service")
@Getter
@Setter
public class UserServiceProperties {

    private String host;
    private String port;
    private String apiKey;
}
