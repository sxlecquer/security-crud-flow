package com.example.client.exception;

// represents an exception that should never occur
public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }
}
