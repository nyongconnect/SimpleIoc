package com.example.simpleioc.config;


import com.example.simpleioc.config.configuration.InstantiationConfiguration;
import com.example.simpleioc.config.configuration.ScanningConfiguration;

public class CustomConfiguration {

    private final ScanningConfiguration annotations;

    private final InstantiationConfiguration instantiations;

    public CustomConfiguration() {
        this.annotations = new ScanningConfiguration(this);
        this.instantiations = new InstantiationConfiguration(this);
    }

    public ScanningConfiguration scanning() {
        return this.annotations;
    }

    public InstantiationConfiguration instantiations() {
        return this.instantiations;
    }

    public CustomConfiguration build() {
        return this;
    }
}
