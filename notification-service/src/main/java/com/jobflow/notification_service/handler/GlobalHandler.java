package com.jobflow.notification_service.handler;

import com.jobflow.notification_service.exception.InvalidTelegramTokenException;
import com.jobflow.notification_service.exception.UserClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(InvalidTelegramTokenException.class)
    public ResponseEntity<ResponseError> invalidTelegramTokenExcHandler(InvalidTelegramTokenException exc) {
        LOGGER.error("[Invalid Token Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.UNAUTHORIZED.value());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseError);
    }
}
