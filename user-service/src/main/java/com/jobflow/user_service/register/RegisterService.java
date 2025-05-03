package com.jobflow.user_service.register;

import org.springframework.web.multipart.MultipartFile;

public interface RegisterService {

    void register(RegisterRequest registerRequest, MultipartFile avatar);

    RegisterResponse confirmCode(ConfirmCodeRequest confirmCodeRequest);

    void resendCode(ResendCodeRequest resendCodeRequest);
}
