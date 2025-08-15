package com.wiremit.forex.exception;

public class ForexServiceException extends RuntimeException {
    public ForexServiceException(String message) {
        super(message);
    }

    public ForexServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
