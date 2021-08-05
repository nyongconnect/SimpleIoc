package com.example.injectorlib.exceptions;

public class AlreadyInitializedException extends RuntimeException {
    public AlreadyInitializedException(String message) {
        super(message);
    }
}
