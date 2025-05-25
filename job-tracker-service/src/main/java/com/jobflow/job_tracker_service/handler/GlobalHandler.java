package com.jobflow.job_tracker_service.handler;

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
