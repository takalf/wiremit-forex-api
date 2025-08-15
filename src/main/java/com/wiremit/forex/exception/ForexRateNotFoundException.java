package com.wiremit.forex.exception;

public class ForexRateNotFoundException extends RuntimeException {
    public ForexRateNotFoundException(String message) {

        super(message);
    }
}
