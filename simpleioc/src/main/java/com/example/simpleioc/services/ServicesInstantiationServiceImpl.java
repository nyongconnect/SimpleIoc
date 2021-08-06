package com.example.simpleioc.services;


import com.example.simpleioc.config.configuration.InstantiationConfiguration;
import com.example.simpleioc.exceptions.ServiceInstantiationException;
import com.example.simpleioc.models.EnqueuedServiceDetails;
import com.example.simpleioc.models.ServiceDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * {@link ServicesInstantiationService} implementation.
 * <p>
 * Responsible for creating the initial instances or all services.
 */
public class ServicesInstantiationServiceImpl implements ServicesInstantiationService {

    private static final String MAX_NUMBER_OF_ALLOWED_ITERATIONS_REACHED = "Maximum number of allowed iterations was reached '%s'. Remaining services: \n %s";

    /**
     * Configuration containing the maximum number or allowed iterations.
     */
    private final InstantiationConfiguration configuration;

    private final ObjectInstantiationService instantiationService;

    private final DependencyResolveService dependencyResolveService;

    /**
     * The storage for all services that are waiting to the instantiated.
     */
    private final LinkedList<EnqueuedServiceDetails> enqueuedServiceDetails;

    /**
     * Internal Dependency container.
     */
    private final DependencyContainer tempContainer;

    public ServicesInstantiationServiceImpl(InstantiationConfiguration configuration,
                                            ObjectInstantiationService instantiationService,
                                            DependencyResolveService dependencyResolveService) {
        this.configuration = configuration;
        this.instantiationService = instantiationService;
        this.dependencyResolveService = dependencyResolveService;
        this.enqueuedServiceDetails = new LinkedList<>();
        this.tempContainer = new DependencyContainerInternal();
    }

    /**
     * Starts looping and for each cycle gets the first element in the enqueuedServiceDetails since they are ordered
     * by the number of constructor params in ASC order.
     * <p>
     * If the {@link EnqueuedServiceDetails} is resolved (all of its dependencies have instances or there are no params)
     * then the service is being instantiated, registered and its beans are also being registered.
     * Otherwise the element is added back to the enqueuedServiceDetails at the last position.
     *
     * @param mappedServices provided services and their details.
     * @return list of all instantiated services and beans.
     * @throws ServiceInstantiationException if maximum number of iterations is reached
     *                                       One iteration is added only of a service has not been instantiated in this cycle.
     */
    @Override
    public Collection<ServiceDetails> instantiateServices(Set<ServiceDetails> mappedServices) throws ServiceInstantiationException {
        this.init(mappedServices);

        int counter = 0;
        final int maxNumberOfIterations = this.configuration.getMaximumAllowedIterations();
        while (!this.enqueuedServiceDetails.isEmpty()) {
            if (counter > maxNumberOfIterations) {
                throw new ServiceInstantiationException(String.format(
                        MAX_NUMBER_OF_ALLOWED_ITERATIONS_REACHED,
                        maxNumberOfIterations,
                        this.enqueuedServiceDetails)
                );
            }

            final EnqueuedServiceDetails enqueuedServiceDetails = this.enqueuedServiceDetails.removeFirst();

            if (this.dependencyResolveService.isServiceResolved(enqueuedServiceDetails)) {
                this.handleServiceResolved(enqueuedServiceDetails);
            } else {
                this.enqueuedServiceDetails.addLast(enqueuedServiceDetails);
                counter++;
            }
        }

        return this.tempContainer.getAllServices();
    }

    private void handleServiceResolved(EnqueuedServiceDetails enqueuedServiceDetails) {
        final ServiceDetails serviceDetails = enqueuedServiceDetails.getServiceDetails();
        final Object[] constructorInstances = enqueuedServiceDetails.getConstructorInstances();

        this.instantiationService.createInstance(
                serviceDetails,
                constructorInstances,
                enqueuedServiceDetails.getFieldInstances()
        );

        this.registerResolvedDependencies(enqueuedServiceDetails);
        this.registerInstantiatedService(serviceDetails);
    }

    /**
     * Adds the newly created service to the list of instantiated services instantiatedServices.
     * Iterated all enqueued services and if one of them relies on that services, adds its instance
     * to them so they can get resolved.
     *
     * @param newlyCreatedService - the created service.
     */
    private void registerInstantiatedService(ServiceDetails newlyCreatedService) {
        this.tempContainer.getAllServices().add(newlyCreatedService);

        for (EnqueuedServiceDetails enqueuedService : this.enqueuedServiceDetails) {
            this.addDependencyIfRequired(enqueuedService, newlyCreatedService);
        }
    }

    private void addDependencyIfRequired(EnqueuedServiceDetails enqueuedService, ServiceDetails newlyCreatedService) {
        if (this.dependencyResolveService.isDependencyRequired(enqueuedService, newlyCreatedService)) {
            this.dependencyResolveService.addDependency(
                    enqueuedService,
                    this.tempContainer.getServiceDetails(
                            newlyCreatedService.getServiceType(),
                            newlyCreatedService.getInstanceName()
                    )
            );

            this.addDependencyIfRequired(enqueuedService, newlyCreatedService);
        }
    }

    private void registerResolvedDependencies(EnqueuedServiceDetails enqueuedServiceDetails) {
        final ServiceDetails serviceDetails = enqueuedServiceDetails.getServiceDetails();

        serviceDetails.setResolvedConstructorParams(enqueuedServiceDetails.getConstructorParams());
        serviceDetails.setResolvedFields(enqueuedServiceDetails.getFieldDependencies());
    }

    /**
     * Adds each service to the enqueuedServiceDetails.
     * Initializes {@link DependencyResolveService}.
     *
     * @param mappedServices set of mapped services and their information.
     */

    private void init(Set<ServiceDetails> mappedServices) {
        this.enqueuedServiceDetails.clear();
        this.tempContainer.init(new ArrayList<>(), new ArrayList<>(), new ObjectInstantiationServiceImpl());

        for (ServiceDetails serviceDetails : mappedServices) {
            this.enqueuedServiceDetails.add(new EnqueuedServiceDetails(serviceDetails));
        }

        for (ServiceDetails instantiatedService : this.configuration.getProvidedServices()) {
            this.registerInstantiatedService(instantiatedService);
        }

        this.dependencyResolveService.init(mappedServices);
        this.dependencyResolveService.checkDependencies(this.enqueuedServiceDetails);
    }
}
