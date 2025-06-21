package com.jobflow.notification_service.email;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailProperties emailProperties;
    private final JavaMailSender javaMailSender;

    @Override
    public void send(String email, String subject, String text) {
        LOGGER.debug("Sending email to: {} with subject: {}", email, subject);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(email);
        simpleMailMessage.setFrom(emailProperties.getUsername());
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);

        javaMailSender.send(simpleMailMessage);
        LOGGER.debug("Email sent successfully to: {} with subject: {}", email, subject);
    }
}
