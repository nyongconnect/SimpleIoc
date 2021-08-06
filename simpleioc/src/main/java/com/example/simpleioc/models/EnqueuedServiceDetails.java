package com.example.simpleioc.models;

import com.example.simpleioc.annotation.Qualifier;
import com.example.simpleioc.utils.AliasFinder;
import com.example.simpleioc.utils.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.LinkedList;

/**
 * Simple POJO class that keeps information about a service, its
 * required dependencies and the ones that are already resolved.
 */
public class EnqueuedServiceDetails {

    /**
     * Reference to the target service.
     */
    private final ServiceDetails serviceDetails;

    /**
     * List of dependencies that the target constructor of the service requires.
     */
    private final LinkedList<DependencyParam> constructorParams;


     // List of dependencies that are required from {@link Inject} annotated fields.

    private final LinkedList<DependencyParam> fieldDependencies;

    public EnqueuedServiceDetails(ServiceDetails serviceDetails) {
        this.serviceDetails = serviceDetails;
        this.constructorParams = new LinkedList<>();
        this.fieldDependencies = new LinkedList<>();
        this.fillConstructorParams();
        this.fillFieldDependencyTypes();
    }

    public ServiceDetails getServiceDetails() {
        return this.serviceDetails;
    }

    public LinkedList<DependencyParam> getConstructorParams() {
        return this.constructorParams;
    }


    public Object[] getConstructorInstances() {
        return this.constructorParams.stream()
                .map(DependencyParam::getInstance)
                .toArray(Object[]::new);
    }

    public LinkedList<DependencyParam> getFieldDependencies() {
        return this.fieldDependencies;
    }

    public Object[] getFieldInstances() {
        return this.fieldDependencies.stream()
                .map(DependencyParam::getInstance)
                .toArray(Object[]::new);
    }

    private void fillConstructorParams() {
        for (Parameter parameter : this.serviceDetails.getTargetConstructor().getParameters()) {
            this.constructorParams.add(new DependencyParam(
                    parameter.getType(),
                    this.getInstanceName(parameter.getDeclaredAnnotations()),
                    parameter.getDeclaredAnnotations()
            ));
        }
    }

    private void fillFieldDependencyTypes() {
        for (Field injectAnnotatedField : this.serviceDetails.getInjectAnnotatedFields()) {
            this.fieldDependencies.add(new DependencyParam(
                    injectAnnotatedField.getType(),
                    this.getInstanceName(injectAnnotatedField.getDeclaredAnnotations()),
                    injectAnnotatedField.getDeclaredAnnotations()
            ));
        }
    }

    private String getInstanceName(Annotation[] annotations) {
        final Annotation annotation = AliasFinder.getAnnotation(annotations, Qualifier.class);

        if (annotation != null) {
            return AnnotationUtils.getAnnotationValue(annotation).toString();
        }

        return null;
    }

    @Override
    public String toString() {
        return this.serviceDetails.getServiceType().getName();
    }
}
