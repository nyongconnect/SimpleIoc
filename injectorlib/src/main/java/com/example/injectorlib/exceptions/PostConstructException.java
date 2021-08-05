package com.example.injectorlib.exceptions;

public class PostConstructException extends ServiceInstantiationException {
    public PostConstructException(String message, Throwable cause) {
        super(message, cause);
    }
}
