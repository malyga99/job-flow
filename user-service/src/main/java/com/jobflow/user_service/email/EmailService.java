package com.jobflow.user_service.email;

public interface EmailService {

    void sendCodeToEmail(String email, int code);

}
