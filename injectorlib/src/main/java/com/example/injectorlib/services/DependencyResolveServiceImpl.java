package com.example.injectorlib.services;


import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.injectorlib.annotation.Inject;
import com.example.injectorlib.config.configuration.InstantiationConfiguration;
import com.example.injectorlib.exceptions.ServiceInstantiationException;
import com.example.injectorlib.handler.DependencyResolver;
import com.example.injectorlib.models.DependencyParam;
import com.example.injectorlib.models.EnqueuedServiceDetails;
import com.example.injectorlib.models.ServiceDetails;
import com.example.injectorlib.utils.AliasFinder;
import com.example.injectorlib.utils.DependencyParamUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyResolveServiceImpl implements DependencyResolveService {

    private static final String COULD_NOT_CREATE_INSTANCE_MISSING_DEPENDENCY_MSG = "Could not create instance of '%s'. Parameter '%s' implementation was not found";
    private static final String COULD_NOT_FIND_QUALIFIER_MSG = "Could not create instance of '%s'. Qualifier '%s' was not found.";

    /**
     * Contains all available types that will be loaded from this service.
     * This includes all services and all beans.
     */
    private final List<Class<?>> allAvailableClasses;

    private final InstantiationConfiguration configuration;

    private Collection<ServiceDetails> mappedServices;

    public DependencyResolveServiceImpl(InstantiationConfiguration configuration) {
        this.allAvailableClasses = new ArrayList<>();
        this.configuration = configuration;
    }

    /**
     * Extracts all available classes from the mapped serviced and adds them into one list.
     *
     * @param mappedServices - located services in the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void init(Collection<ServiceDetails> mappedServices) {
        this.allAvailableClasses.clear();
        this.mappedServices = mappedServices;

        for (ServiceDetails serviceDetails : this.mappedServices) {
            this.allAvailableClasses.add(serviceDetails.getServiceType());
        }

        //If services are provided through config, add them to the list of available classes and instances.
        this.allAvailableClasses.addAll(this.configuration.getProvidedServices()
                .stream()
                .map(ServiceDetails::getServiceType)
                .collect(Collectors.toList())
        );
    }

    /**
     * Iterate through all constructor params and all {@link Inject} fields and check if their types can be satisfied.
     * If the user has provided {@link DependencyResolver}, the value will be set.
     * <p>
     *
     * @param enqueuedServiceDetails - given collection of services.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void checkDependencies(Collection<EnqueuedServiceDetails> enqueuedServiceDetails) throws ServiceInstantiationException {
        for (EnqueuedServiceDetails enqueuedService : enqueuedServiceDetails) {
            final Class<?> serviceType = enqueuedService.getServiceDetails().getServiceType();

            this.checkDependencyParameters(serviceType, enqueuedService.getConstructorParams());
            this.checkDependencyParameters(serviceType, enqueuedService.getFieldDependencies());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkDependencyParameters(Class<?> serviceType, Collection<DependencyParam> dependencyParams) {
        for (DependencyParam dependencyParam : dependencyParams) {
            final Class<?> dependencyType = dependencyParam.getDependencyType();

            if (AliasFinder.isAnnotationPresent(dependencyParam.getAnnotations(), Nullable.class)) {
                dependencyParam.setRequired(false);
            }

            if (dependencyParam.getInstanceName() != null) {
                if (this.isNamedInstancePresent(dependencyType, dependencyParam.getInstanceName())) {
                    dependencyParam.setValuePresent(true);
                    continue;
                }

                if (dependencyParam.isRequired()) {
                    throw new ServiceInstantiationException(String.format(
                            COULD_NOT_FIND_QUALIFIER_MSG, serviceType.getName(), dependencyParam.getInstanceName()
                    ));
                }

                continue;
            }

            if (this.isAssignableTypePresent(dependencyType)) {
                dependencyParam.setValuePresent(true);
                continue;
            }

            final DependencyResolver dependencyResolver = this.getDependencyResolver(dependencyParam);
            if (dependencyResolver != null) {
                dependencyParam.setInstance(dependencyResolver.resolve(dependencyParam));
                dependencyParam.setDependencyResolver(dependencyResolver);
                dependencyParam.setValuePresent(true);
                continue;
            }

            if (dependencyParam.isRequired()) {
                throw new ServiceInstantiationException(
                        String.format(COULD_NOT_CREATE_INSTANCE_MISSING_DEPENDENCY_MSG,
                                serviceType.getName(),
                                dependencyType.getName()
                        )
                );
            }
        }
    }

    /**
     * Adds the object instance in the array of instantiated dependencies
     * by keeping the exact same position as the target constructor of the service has it.
     */
    @Override
    public void addDependency(EnqueuedServiceDetails enqueuedServiceDetails, ServiceDetails serviceDetails) {
        if (!this.addDependency(enqueuedServiceDetails.getConstructorParams(), serviceDetails)) {
            this.addDependency(enqueuedServiceDetails.getFieldDependencies(), serviceDetails);
        }
    }

    private boolean addDependency(Collection<DependencyParam> dependencyParams, ServiceDetails serviceDetails) {
        for (DependencyParam dependencyParam : dependencyParams) {
            if (DependencyParamUtils.isDependencyRequired(dependencyParam, serviceDetails)) {
                dependencyParam.setInstance(serviceDetails.getInstance());
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if all dependencies have corresponding instances.
     *
     * @param serviceDetails - given service details.
     * @return true of all dependency instances are available.
     */
    @Override
    public boolean isServiceResolved(EnqueuedServiceDetails serviceDetails) {
        return DependencyParamUtils.dependencyParamsResolved(serviceDetails.getConstructorParams()) &&
                DependencyParamUtils.dependencyParamsResolved(serviceDetails.getFieldDependencies());
    }

    @Override
    public boolean isDependencyRequired(EnqueuedServiceDetails enqueuedServiceDetails, ServiceDetails serviceDetails) {
        return DependencyParamUtils.isDependencyRequired(enqueuedServiceDetails.getConstructorParams(), serviceDetails) ||
                DependencyParamUtils.isDependencyRequired(enqueuedServiceDetails.getFieldDependencies(), serviceDetails);
    }

    /**
     * @param cls given type.
     * @return true if allAvailableClasses contains a type
     * that is compatible with the given type.
     */
    private boolean isAssignableTypePresent(Class<?> cls) {
        for (Class<?> serviceType : this.allAvailableClasses) {
            if (cls.isAssignableFrom(serviceType)) {
                return true;
            }
        }

        return false;
    }

    private boolean isNamedInstancePresent(Class<?> cls, String nameOfInstance) {
        return this.isNamedInstancePresent(cls, nameOfInstance, this.mappedServices) ||
                this.isNamedInstancePresent(cls, nameOfInstance, this.configuration.getProvidedServices());
    }

    private boolean isNamedInstancePresent(Class<?> cls, String nameOfInstance, Collection<ServiceDetails> serviceDetails) {
        for (ServiceDetails providedService : serviceDetails) {
            if (nameOfInstance.equalsIgnoreCase(providedService.getInstanceName()) &&
                    cls.isAssignableFrom(providedService.getServiceType())) {
                return true;
            }
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private DependencyResolver getDependencyResolver(DependencyParam dependencyParam) {
        return this.configuration.getDependencyResolvers().stream()
                .filter(dr -> dr.canResolve(dependencyParam))
                .findFirst().orElse(null);
    }
}
