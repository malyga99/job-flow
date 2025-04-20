package com.jobflow.user_service.handler;

import com.jobflow.user_service.exception.UserNotFoundException;
import org.apache.tomcat.websocket.AuthenticationException;
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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseError> userNotFoundExcHandler(UserNotFoundException exc) {
        LOGGER.error("[User Not Found Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.NOT_FOUND.value());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> methodArgumentNotValidExcHandler(MethodArgumentNotValidException exc) {
        String errorMessages = exc.getFieldErrors().stream()
                .map(el -> "%s: %s".formatted(el.getField(), el.getDefaultMessage()))
                .collect(joining(", "));
        LOGGER.error("Validation Exception: {}", errorMessages);

        ResponseError responseError = ResponseError.buildResponseError(errorMessages, HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }


}
