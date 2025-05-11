package com.jobflow.user_service.openIdGithub;

import com.jobflow.user_service.exception.StateValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubOpenIdStateValidatorTest {

    @Mock
    private GitHubOpenIdProperties openIdProperties;

    @InjectMocks
    private GitHubOpenIdStateValidator openIdStateValidator;

    @Test
    public void validateState_validState_doNothing() {
        when(openIdProperties.getState()).thenReturn("valid-state");

        assertDoesNotThrow(() -> openIdStateValidator.validateState("valid-state"));
    }

    @Test
    public void validateState_invalidState_throwExc() {
        String invalidState = "invalidState";
        when(openIdProperties.getState()).thenReturn("valid-state");

        var stateValidationException = assertThrows(StateValidationException.class, () -> openIdStateValidator.validateState(invalidState));
        assertEquals("State: " + invalidState + " not valid", stateValidationException.getMessage());
    }
}