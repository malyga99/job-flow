package com.jobflow.user_service.email;

import com.jobflow.user_service.register.RegisterRequest;

public interface EmailVerificationService {

    void sendVerificationCode(RegisterRequest registerRequest);
}
