package com.jobflow.notification_service.email;

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

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private EmailProperties emailProperties;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    public void send_successfullySendEmailWithCorrectlyDetails() {
        ArgumentCaptor<SimpleMailMessage> argumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        when(emailProperties.getUsername()).thenReturn("Sender-username");

        emailService.send("Recipient-username", "test-subject", "test-text");

        verify(javaMailSender, times(1)).send(argumentCaptor.capture());

        SimpleMailMessage simpleMailMessage = argumentCaptor.getValue();
        assertEquals("Recipient-username", simpleMailMessage.getTo()[0]);
        assertEquals("Sender-username", simpleMailMessage.getFrom());
        assertEquals("test-subject", simpleMailMessage.getSubject());
        assertEquals("test-text", simpleMailMessage.getText());
    }

}