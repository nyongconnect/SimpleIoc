package com.example.injectorlib.services;

import android.annotation.SuppressLint;

import com.example.injectorlib.annotation.Component;
import com.example.injectorlib.annotation.Inject;
import com.example.injectorlib.annotation.Name;
import com.example.injectorlib.annotation.Scope;
import com.example.injectorlib.config.configuration.ScanningConfiguration;
import com.example.injectorlib.enums.ScopeType;
import com.example.injectorlib.event.ServiceDetailsCreated;
import com.example.injectorlib.models.ServiceDetails;
import com.example.injectorlib.utils.AliasFinder;
import com.example.injectorlib.utils.AnnotationUtils;
import com.example.injectorlib.utils.ServiceDetailsConstructComparator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link ServicesScanningService} implementation.
 * Iterates all located classes and looks for classes with @Component
 * annotation or one provided by the client and then collects data for that class.
 */
public class ServicesScanningServiceImpl implements ServicesScanningService {

    /**
     * Configuration containing annotations provided by the client.
     */
    private final ScanningConfiguration configuration;

    public ServicesScanningServiceImpl(ScanningConfiguration configuration) {
        this.configuration = configuration;
        this.init();
    }

    /**
     * Iterates scanned classes with @{@link Component} or user specified annotation
     * and creates a {@link ServiceDetails} object with the collected information.
     *
     * @param locatedClasses given set of classes.
     * @return set or services and their collected details.
     */
    @SuppressLint("NewApi")
    @Override
    public Set<ServiceDetails> mapServices(Set<Class<?>> locatedClasses) {
        final Map<Class<?>, Annotation> onlyServiceClasses = this.filterServiceClasses(locatedClasses);

        final Set<ServiceDetails> serviceDetailsStorage = new HashSet<>();

        for (Map.Entry<Class<?>, Annotation> serviceAnnotationEntry : onlyServiceClasses.entrySet()) {
            final Class<?> cls = serviceAnnotationEntry.getKey();
            final Annotation annotation = serviceAnnotationEntry.getValue();

            final ServiceDetails serviceDetails = new ServiceDetails(
                    cls,
                    annotation,
                    this.findSuitableConstructor(cls),
                    this.findInstanceName(cls.getDeclaredAnnotations()),
                    this.findScope(cls),
                    this.findAutowireAnnotatedFields(cls, new ArrayList<>()).toArray(new Field[0])
            );

            this.notifyServiceDetailsCreated(serviceDetails);

            serviceDetailsStorage.add(serviceDetails);
        }

        return serviceDetailsStorage.stream()
                .sorted(new ServiceDetailsConstructComparator())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Iterates all given classes and filters those that have {@link Component} annotation
     * or one prided by the client.
     *
     * @return service annotated classes.
     */
    @SuppressLint("NewApi")
    private Map<Class<?>, Annotation> filterServiceClasses(Collection<Class<?>> scannedClasses) {
        final Set<Class<? extends Annotation>> serviceAnnotations = this.configuration.getCustomServiceAnnotations();
        final Map<Class<?>, Annotation> locatedClasses = new HashMap<>();

        for (Class<?> cls : scannedClasses) {
            if (cls.isInterface() || cls.isEnum() || cls.isAnnotation()) {
                continue;
            }

            for (Annotation annotation : cls.getAnnotations()) {
                if (serviceAnnotations.contains(annotation.annotationType())) {
                    locatedClasses.put(cls, annotation);
                    break;
                }
            }
        }

        this.configuration.getAdditionalClasses().forEach((cls, a) -> {
            Annotation annotation = null;
            if (a != null && cls.isAnnotationPresent(a)) {
                annotation = cls.getAnnotation(a);
            }

            locatedClasses.put(cls, annotation);
        });

        return locatedClasses;
    }

    /**
     * Looks for a constructor from the given class that has {@link Inject} annotation
     * or gets the first one.
     *
     * @param cls - the given class.
     * @return suitable constructor.
     */
    private Constructor<?> findSuitableConstructor(Class<?> cls) {
        for (Constructor<?> ctr : cls.getDeclaredConstructors()) {
            if (AliasFinder.isAnnotationPresent(ctr.getDeclaredAnnotations(), Inject.class)) {
                ctr.setAccessible(true);
                return ctr;
            }
        }

        return cls.getConstructors()[0];
    }
    private void notifyServiceDetailsCreated(ServiceDetails serviceDetails) {
        for (ServiceDetailsCreated callback : this.configuration.getServiceDetailsCreatedCallbacks()) {
            callback.serviceDetailsCreated(serviceDetails);
        }
    }

    /**
     * Search for {@link Scope} annotation within the class and get it's value.
     *
     * @param cls - given class.
     * @return the value of the annotation or SINGLETON as default.
     */
    @SuppressLint("NewApi")
    private ScopeType findScope(Class<?> cls) {
        if (cls.isAnnotationPresent(Scope.class)) {
            return cls.getDeclaredAnnotation(Scope.class).value();
        }

        return ScopeType.DEFAULT_SCOPE;
    }

    /**
     * Search for {@link Scope} annotation within the method and get it's value.
     *
     * @param method - given bean method.
     * @return the value of the annotation or SINGLETON as default.
     */
    @SuppressLint("NewApi")
    private ScopeType findScope(Method method) {
        if (method.isAnnotationPresent(Scope.class)) {
            return method.getDeclaredAnnotation(Scope.class).value();
        }

        return ScopeType.DEFAULT_SCOPE;
    }

    private String findInstanceName(Annotation[] annotations) {
        if (!AliasFinder.isAnnotationPresent(annotations, Name.class)) {
            return null;
        }

        final Annotation annotation = AliasFinder.getAnnotation(annotations, Name.class);

        return AnnotationUtils.getAnnotationValue(annotation).toString();
    }

    private List<Field> findAutowireAnnotatedFields(Class<?> cls, List<Field> fields) {
        for (Field declaredField : cls.getDeclaredFields()) {
            if (AliasFinder.isAnnotationPresent(declaredField.getDeclaredAnnotations(), Inject.class)) {
                declaredField.setAccessible(true);
                fields.add(declaredField);
            }
        }

        if (cls.getSuperclass() != null) {
            return this.findAutowireAnnotatedFields(cls.getSuperclass(), fields);
        }

        return fields;
    }

    /**
     * Adds the platform's default annotations for services and beans on top of the
     * ones that the client might have provided.
     */
    private void init() {
        this.configuration.getCustomServiceAnnotations().add(Component.class);
    }
}
