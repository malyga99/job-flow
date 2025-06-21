package com.jobflow.notification_service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class EmailConfiguration {

    private static final int TIMEOUT_MILLIS = 10000;

    private final EmailProperties emailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(emailProperties.getHost());
        javaMailSender.setPort(Integer.parseInt(emailProperties.getPort()));
        javaMailSender.setUsername(emailProperties.getUsername());
        javaMailSender.setPassword(emailProperties.getPassword());
        javaMailSender.setProtocol(emailProperties.getProtocol());

        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.starttls.required", true);
        properties.put("mail.smtp.ssl.trust", emailProperties.getHost());
        properties.put("mail.smtp.connectiontimeout", TIMEOUT_MILLIS);
        properties.put("mail.smtp.timeout", TIMEOUT_MILLIS);
        properties.put("mail.smtp.writetimeout", TIMEOUT_MILLIS);

        return javaMailSender;
    }
}
