package com.jobflow.notification_service.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.mail")
@Getter
@Setter
public class EmailProperties {

    private String host;
    private String port;
    private String username;
    private String password;
    private String protocol;
}
