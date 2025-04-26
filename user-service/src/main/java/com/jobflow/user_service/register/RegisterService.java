package com.jobflow.user_service.register;

import jakarta.validation.Valid;

public interface RegisterService {

    void register(RegisterRequest registerRequest);

    RegisterResponse confirmCode(ConfirmCodeRequest confirmCodeRequest);
}
