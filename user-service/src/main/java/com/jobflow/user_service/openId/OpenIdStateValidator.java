package com.jobflow.user_service.openId;

public interface OpenIdStateValidator {

    void validateState(String state);
}
