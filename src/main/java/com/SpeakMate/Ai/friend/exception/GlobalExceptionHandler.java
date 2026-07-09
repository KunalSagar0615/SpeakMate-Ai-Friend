package com.SpeakMate.Ai.friend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), false),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex) {

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), false),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUsernameAlreadyExistsException(
            UsernameAlreadyExistsException ex) {

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), false),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse> handleInvalidOtpException(
            InvalidOtpException ex) {

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), false),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiResponse> handleOtpExpiredException(
            OtpExpiredException ex) {

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), false),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MaximumOtpAttemptsExceededException.class)
    public ResponseEntity<ApiResponse> handleMaximumOtpAttemptsExceededException(
            MaximumOtpAttemptsExceededException ex) {

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), false),
                HttpStatus.TOO_MANY_REQUESTS
        );
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse> handleEmailNotVerifiedException(
            EmailNotVerifiedException ex) {

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), false),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        return new ResponseEntity<>(
                new ApiResponse(message, false),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(
            Exception ex) {

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFoundException(
            UserNotFoundException ex
    ) {

        ApiResponse response = new ApiResponse(
                ex.getMessage(),
                false
        );

        return new ResponseEntity<>(
                response,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex
    ) {

        ApiResponse response = new ApiResponse(
                ex.getMessage(),
                false
        );

        return new ResponseEntity<>(
                response,
                HttpStatus.UNAUTHORIZED
        );
    }
}