package com.example.simpleioc.enums;

public enum ScopeType {
    /**
     * Single instance for the whole lifespan of the app.
     */
    SINGLETON,

    /**
     * New instance for each request to obtain the service.
     */
    PROTOTYPE;

    public static final ScopeType DEFAULT_SCOPE = SINGLETON;
}
