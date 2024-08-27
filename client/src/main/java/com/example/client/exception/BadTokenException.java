package com.example.client.exception;

public class BadTokenException extends RuntimeException {
    public BadTokenException(String message) {
        super(message);
    }
}
