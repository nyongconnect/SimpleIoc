package com.example.simpleioc.config.configuration;


import com.example.simpleioc.config.BaseSubConfiguration;
import com.example.simpleioc.config.CustomConfiguration;
import com.example.simpleioc.event.ServiceDetailsCreated;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScanningConfiguration extends BaseSubConfiguration {

    private final Set<Class<? extends Annotation>> customServiceAnnotations;

    private final Set<Class<? extends Annotation>> customBeanAnnotations;

    private final Map<Class<?>, Class<? extends Annotation>> additionalClasses;

    private final Set<ServiceDetailsCreated> serviceDetailsCreatedCallbacks;

    private ClassLoader classLoader;

    public ScanningConfiguration(CustomConfiguration parentConfig) {
        super(parentConfig);
        this.customServiceAnnotations = new HashSet<>();
        this.customBeanAnnotations = new HashSet<>();
        this.additionalClasses = new HashMap<>();
        this.serviceDetailsCreatedCallbacks = new HashSet<>();
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public ScanningConfiguration addCustomServiceAnnotation(Class<? extends Annotation> annotation) {
        this.customServiceAnnotations.add(annotation);
        return this;
    }

    public ScanningConfiguration addCustomServiceAnnotations(Collection<Class<? extends Annotation>> annotations) {
        this.customServiceAnnotations.addAll(annotations);
        return this;
    }

    public ScanningConfiguration addAdditionalClassesForScanning(Map<Class<?>, Class<? extends Annotation>> additionalClasses) {
        this.additionalClasses.putAll(additionalClasses);
        return this;
    }

    public ScanningConfiguration addServiceDetailsCreatedCallback(ServiceDetailsCreated serviceDetailsCreated) {
        this.serviceDetailsCreatedCallbacks.add(serviceDetailsCreated);
        return this;
    }

    public ScanningConfiguration setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public Set<Class<? extends Annotation>> getCustomServiceAnnotations() {
        return this.customServiceAnnotations;
    }

    public Map<Class<?>, Class<? extends Annotation>> getAdditionalClasses() {
        return this.additionalClasses;
    }

    public Set<ServiceDetailsCreated> getServiceDetailsCreatedCallbacks() {
        return this.serviceDetailsCreatedCallbacks;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
}
