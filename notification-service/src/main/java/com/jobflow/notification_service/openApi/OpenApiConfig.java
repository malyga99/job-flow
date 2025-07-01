package com.jobflow.notification_service.openApi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = @Server(
                url = "http://localhost:8082"
        ),
        info = @Info(
                title = "Notification service",
                description = "Notification service API documentation",
                version = "1.0.0",
                contact = @Contact(
                        name = "Mikhail Malygin",
                        email = "mikhail.malygin.2024@gmail.com",
                        url = "https://github.com/malyga99/job-flow"
                )
        )
)
@Configuration
public class OpenApiConfig {
}
