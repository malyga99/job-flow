package com.jobflow.user_service.handler;

import com.jobflow.user_service.exception.*;
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

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ResponseError> userAlreadyExistsExcHandler(UserAlreadyExistsException exc) {
        LOGGER.error("[User Already Exists Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.CONFLICT.value());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseError);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ResponseError> tooManyRequestsExcHandler(TooManyRequestsException exc) {
        LOGGER.error("[Too Many Requests Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.TOO_MANY_REQUESTS.value());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(responseError);
    }

    @ExceptionHandler(TokenRevokedException.class)
    public ResponseEntity<ResponseError> tokenRevokedExcHandler(TokenRevokedException exc) {
        LOGGER.error("[Token Revoked Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.UNAUTHORIZED.value());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseError);
    }

    @ExceptionHandler(EmailServiceException.class)
    public ResponseEntity<ResponseError> emailServiceExcHandler(EmailServiceException exc) {
        LOGGER.error("[Email Service Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }

    @ExceptionHandler(FileServiceException.class)
    public ResponseEntity<ResponseError> fileServiceExcHandler(FileServiceException exc) {
        LOGGER.error("[File Service Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }

    @ExceptionHandler(OpenIdServiceException.class)
    public ResponseEntity<ResponseError> openIdServiceExcHandler(OpenIdServiceException exc) {
        LOGGER.error("[OpenID Service Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ResponseError> verificationCodeExpiredExcHandler(VerificationCodeExpiredException exc) {
        LOGGER.error("[Verification Code Expired Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.GONE.value());

        return ResponseEntity.status(HttpStatus.GONE).body(responseError);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ResponseError> invalidVerificationCodeExcHandler(InvalidVerificationCodeException exc) {
        LOGGER.error("[Invalid Verification Code Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }

    @ExceptionHandler(UnsupportedProviderException.class)
    public ResponseEntity<ResponseError> unsupportedProviderExcHandler(UnsupportedProviderException exc) {
        LOGGER.error("[Unsupported Provider Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }

    @ExceptionHandler(StateValidationException.class)
    public ResponseEntity<ResponseError> stateValidationExcHandler(StateValidationException exc) {
        LOGGER.error("[State Validation Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }

    @ExceptionHandler(IdTokenValidationException.class)
    public ResponseEntity<ResponseError> idTokenValidationExcHandler(IdTokenValidationException exc) {
        LOGGER.error("[Id Token Validation Exception]: {}", exc.getMessage());
        ResponseError responseError = ResponseError.buildResponseError(exc.getMessage(), HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
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
