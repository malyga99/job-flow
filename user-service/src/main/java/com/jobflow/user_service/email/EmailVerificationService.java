package com.jobflow.user_service.email;

import com.jobflow.user_service.register.ConfirmCodeRequest;
import com.jobflow.user_service.register.RegisterRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public interface EmailVerificationService {

    void sendVerificationCode(RegisterRequest registerRequest);

    RegisterRequest validateVerificationCode(ConfirmCodeRequest confirmCodeRequest);
}
