package com.example.injectorlib.config;

public abstract class BaseSubConfiguration {

    private final CustomConfiguration parentConfig;

    protected BaseSubConfiguration(CustomConfiguration parentConfig) {
        this.parentConfig = parentConfig;
    }

    public CustomConfiguration and() {
        return this.parentConfig;
    }
}
