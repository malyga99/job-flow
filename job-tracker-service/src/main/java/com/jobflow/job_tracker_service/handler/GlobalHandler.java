package com.jobflow.job_tracker_service.handler;

import com.jobflow.job_tracker_service.exception.JobApplicationNotFoundException;
import com.jobflow.job_tracker_service.exception.JobApplicationServiceException;
import com.jobflow.job_tracker_service.exception.UserDontHavePermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static java.util.stream.Collectors.joining;

@RestControllerAdvice
public class GlobalHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(JobApplicationNotFoundException.class)
    public ResponseEntity<ResponseError> jobApplicationNotFoundExcHandler(JobApplicationNotFoundException exc) {
        LOGGER.error("[Job Application Not Found Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.NOT_FOUND.value());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }

    @ExceptionHandler(JobApplicationServiceException.class)
    public ResponseEntity<ResponseError> jobApplicationServiceExcHandler(JobApplicationServiceException exc) {
        LOGGER.error("[Job Application Service Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }

    @ExceptionHandler(UserDontHavePermissionException.class)
    public ResponseEntity<ResponseError> userDontHavePermissionExcHandler(UserDontHavePermissionException exc) {
        LOGGER.error("[User Dont Have Permission Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.FORBIDDEN.value());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> methodArgumentNotValidExcHandler(MethodArgumentNotValidException exc) {
        String errorMessages = exc.getFieldErrors().stream()
                .map(el -> "%s: %s".formatted(el.getField(), el.getDefaultMessage()))
                .collect(joining(", "));
        LOGGER.error("[Validation Exception]: {}", errorMessages);

        ResponseError responseError = ResponseError.buildResponseError(errorMessages, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }
}
