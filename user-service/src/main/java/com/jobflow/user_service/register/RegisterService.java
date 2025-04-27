package com.jobflow.user_service.register;

public interface RegisterService {

    void register(RegisterRequest registerRequest);

    RegisterResponse confirmCode(ConfirmCodeRequest confirmCodeRequest);

    void resendCode(ResendCodeRequest resendCodeRequest);
}
