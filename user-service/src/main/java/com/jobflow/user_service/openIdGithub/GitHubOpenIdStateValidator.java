package com.jobflow.user_service.openIdGithub;

import com.jobflow.user_service.exception.StateValidationException;
import com.jobflow.user_service.openId.OpenIdStateValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubOpenIdStateValidator implements OpenIdStateValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubOpenIdStateValidator.class);
    private final GitHubOpenIdProperties openIdProperties;

    @Override
    public void validateState(String state) {
        LOGGER.debug("Starting state validation: {}", state);
        String githubState = openIdProperties.getState();

        if (!githubState.equals(state)) {
            throw new StateValidationException("State: " + state + " not valid");
        }

        LOGGER.debug("Successfully state validation: {}", state);
    }
}
