package com.wiremit.forex.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private LocalDateTime timestamp;
    private String path;
    private int status;

    // Private constructor to enforce using builder methods
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Success response with data
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.status = 200;
        return response;
    }

    // Success response with data and custom message
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        response.status = 200;
        return response;
    }

    // Success response with only message (no data)
    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.status = 200;
        return response;
    }

    // Error response with a message
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.status = 400;
        return response;
    }

    // Error response with message and status
    public static <T> ApiResponse<T> error(String message, int status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.status = status;
        return response;
    }

    // Error response with multiple errors
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.errors = errors;
        response.status = 400;
        return response;
    }

    // Error response with multiple errors and status
    public static <T> ApiResponse<T> error(String message, List<String> errors, int status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.errors = errors;
        response.status = status;
        return response;
    }

    // Builder pattern methods for chaining
    public ApiResponse<T> path(String path) {
        this.path = path;
        return this;
    }

    public ApiResponse<T> status(int status) {
        this.status = status;
        return this;
    }
}
