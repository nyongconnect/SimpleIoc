package com.example.injectorlib.models;


import com.example.injectorlib.annotation.Inject;
import com.example.injectorlib.enums.ScopeType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Simple POJO class that holds information about a given class.
 * <p>
 * This is needed since that way we have the data scanned only once and we
 * will improve performance at runtime since the data in only collected once
 * at startup.
 */
public class ServiceDetails {

    /**
     * The type of the service.
     */
    private Class<?> serviceType;

    /**
     * The annotation used to map the service (@Component or a custom one).
     */
    private Annotation annotation;

    /**
     * The constructor that will be used to create an instance of the service.
     */
    private Constructor<?> targetConstructor;

    /**
     * The name of the instance or null if no name has been given.
     */
    private String instanceName;

    /**
     * Component instance.
     */
    private Object instance;

    /**
     * Holds information for service's scope.
     */
    private ScopeType scopeType;

    /**
     * Array of fields within the service that are annotated with @{@link Inject}
     */
    private Field[] autowireAnnotatedFields;

    /**
     * Collection with details about resolved dependencies from the target constructor.
     */
    private LinkedList<DependencyParam> resolvedConstructorParams;

    /**
     * Collection with details about resolved {@link Inject} field dependencies.
     */
    private LinkedList<DependencyParam> resolvedFields;

    protected ServiceDetails() {

    }

    public ServiceDetails(Class<?> serviceType,
                          Annotation annotation, Constructor<?> targetConstructor,
                          String instanceName,
                          ScopeType scopeType,
                          Field[] injectAnnotatedFields) {
        this();
        this.setServiceType(serviceType);
        this.setAnnotation(annotation);
        this.setTargetConstructor(targetConstructor);
        this.setInstanceName(instanceName);
        this.setScopeType(scopeType);
        this.setInjectAnnotatedFields(injectAnnotatedFields);
    }

    public Class<?> getServiceType() {
        return this.serviceType;
    }

    void setServiceType(Class<?> serviceType) {
        this.serviceType = serviceType;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public Constructor<?> getTargetConstructor() {
        return this.targetConstructor;
    }

    public void setTargetConstructor(Constructor<?> targetConstructor) {
        this.targetConstructor = targetConstructor;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Object getActualInstance() {
        return this.instance;
    }

    public Object getInstance() {
        return this.instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public ScopeType getScopeType() {
        return this.scopeType;
    }

    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public Field[] getInjectAnnotatedFields() {
        return this.autowireAnnotatedFields;
    }

    public void setInjectAnnotatedFields(Field[] autowireAnnotatedFields) {
        this.autowireAnnotatedFields = autowireAnnotatedFields;
    }

    public LinkedList<DependencyParam> getResolvedConstructorParams() {
        return this.resolvedConstructorParams;
    }

    public void setResolvedConstructorParams(LinkedList<DependencyParam> resolvedConstructorParams) {
        this.resolvedConstructorParams = resolvedConstructorParams;
    }

    public LinkedList<DependencyParam> getResolvedFields() {
        return this.resolvedFields;
    }

    public void setResolvedFields(LinkedList<DependencyParam> resolvedFields) {
        this.resolvedFields = resolvedFields;
    }

    /**
     * We are using the serviceType hashcode in order to make this class unique
     * when using in in sets.
     *
     * @return hashcode.
     */
    @Override
    public int hashCode() {
        if (this.serviceType == null) {
            return super.hashCode();
        }

        return this.serviceType.hashCode();
    }

    @Override
    public String toString() {
        if (this.serviceType == null) {
            return super.toString();
        }

        return this.serviceType.getName();
    }
}
