package com.jobflow.user_service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    private static final String EMAIL_SENDER = "SenderUsername@gmail.com";
    private static final String EMAIL_RECIPIENT = "IvanIvanov@gmail.com";
    private static final int code = 111111;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private EmailProperties emailProperties;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    public void sendCodeToEmail_successfullySendCodeWithCorrectlyDetails() {
        ArgumentCaptor<SimpleMailMessage> argumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        when(emailProperties.getUsername()).thenReturn(EMAIL_SENDER);

        emailService.sendCodeToEmail(EMAIL_RECIPIENT, code);

        verify(javaMailSender, times(1)).send(argumentCaptor.capture());

        SimpleMailMessage simpleMailMessage = argumentCaptor.getValue();
        assertEquals(EMAIL_RECIPIENT, simpleMailMessage.getTo()[0]);
        assertEquals(EMAIL_SENDER, simpleMailMessage.getFrom());
        assertEquals("Registration confirmation code", simpleMailMessage.getSubject());
        String text = simpleMailMessage.getText();
        assertTrue(text.contains("Hi!") && text.contains("Your confirmation code") && text.contains("JobFlow team") && text.contains(String.valueOf(code)));
    }

}