package com.example.injectorlib.exceptions;

public class PreDestroyExecutionException extends RuntimeException {
    public PreDestroyExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
