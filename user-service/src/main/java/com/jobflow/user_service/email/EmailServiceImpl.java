package com.jobflow.user_service.email;

import lombok.RequiredArgsConstructor;
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

    @Override
    public void sendCodeToEmail(String email, int code) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(email);
        simpleMailMessage.setFrom(emailProperties.getUsername());
        simpleMailMessage.setSubject(LETTER_SUBJECT);
        simpleMailMessage.setText(String.format(LETTER_TEXT, code));

        javaMailSender.send(simpleMailMessage);
    }
}
