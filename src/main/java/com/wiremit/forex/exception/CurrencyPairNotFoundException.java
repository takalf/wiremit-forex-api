package com.wiremit.forex.exception;

public class CurrencyPairNotFoundException extends RuntimeException {
    public CurrencyPairNotFoundException(String message) {

        super(message);
    }
}
