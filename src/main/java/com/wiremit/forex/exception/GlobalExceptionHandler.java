package com.wiremit.forex.exception;

import com.wiremit.forex.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse
                .error("User not found", HttpStatus.NOT_FOUND.value())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse
                .error("Invalid username or password", HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse
                .error("Access denied", HttpStatus.FORBIDDEN.value())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName + ": " + errorMessage);
        });

        ApiResponse<Object> response = ApiResponse
                .error("Validation failed", errors, HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(CurrencyPairNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCurrencyPairNotFound(
            CurrencyPairNotFoundException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse
                .error(ex.getMessage(), HttpStatus.NOT_FOUND.value())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ForexRateNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleForexRateNotFound(
            ForexRateNotFoundException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse
                .error(ex.getMessage(), HttpStatus.NOT_FOUND.value())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ForexServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleForexServiceException(
            ForexServiceException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse
                .error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse
                .error(ex.getMessage(), HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse
                .error("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}