package com.jobflow.user_service.openIdGoogle;

import com.jobflow.user_service.exception.StateValidationException;
import com.jobflow.user_service.openId.OpenIdStateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOpenIdStateValidator implements OpenIdStateValidator {

    private final GoogleOpenIdProperties openIdProperties;

    @Override
    public void validateState(String state) {
        String googleState = openIdProperties.getState();

        if (!googleState.equals(state)) {
            throw new StateValidationException("State: " + state + " not valid");
        }
    }
}
