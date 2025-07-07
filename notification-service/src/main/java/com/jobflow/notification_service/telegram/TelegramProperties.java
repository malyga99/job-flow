package com.jobflow.notification_service.telegram;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@ConfigurationProperties(prefix = "telegram")
@Component
@Getter
@Setter
public class TelegramProperties {

    private String botToken;
    private String botSecretToken;
}
