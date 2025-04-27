package com.jobflow.user_service.email;

import com.jobflow.user_service.register.ConfirmCodeRequest;
import com.jobflow.user_service.register.RegisterRequest;
import com.jobflow.user_service.register.ResendCodeRequest;

public interface EmailVerificationService {

    void sendVerificationCode(RegisterRequest registerRequest);

    RegisterRequest validateVerificationCode(ConfirmCodeRequest confirmCodeRequest);

    void resendCode(ResendCodeRequest resendCodeRequest);
}
