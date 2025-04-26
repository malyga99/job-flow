package com.jobflow.user_service.email;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;
    private static final String LETTER_SUBJECT = "Registration confirmation code";
    private static final String LETTER_TEXT = """
        Hi!

        Your confirmation code: %d
        It will be valid for 5 minutes.

        Thank you,
        JobFlow team
        """;
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendCodeToEmail(String email, int code) {
        LOGGER.debug("Sending of verification code to email: {}", email);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(email);
        simpleMailMessage.setFrom(emailProperties.getUsername());
        simpleMailMessage.setSubject(LETTER_SUBJECT);
        simpleMailMessage.setText(String.format(LETTER_TEXT, code));

        javaMailSender.send(simpleMailMessage);
        LOGGER.debug("Verification code sent successfully to email: {}", email);
    }
}
