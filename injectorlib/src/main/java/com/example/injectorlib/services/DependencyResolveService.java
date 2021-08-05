package com.example.injectorlib.services;

import com.example.injectorlib.exceptions.ServiceInstantiationException;
import com.example.injectorlib.models.EnqueuedServiceDetails;
import com.example.injectorlib.models.ServiceDetails;

import java.util.Collection;

public interface DependencyResolveService {

    void init(Collection<ServiceDetails> mappedServices);

    void checkDependencies(Collection<EnqueuedServiceDetails> enqueuedServiceDetails) throws ServiceInstantiationException;

    void addDependency(EnqueuedServiceDetails enqueuedServiceDetails, ServiceDetails serviceDetails);

    boolean isServiceResolved(EnqueuedServiceDetails enqueuedServiceDetails);

    boolean isDependencyRequired(EnqueuedServiceDetails enqueuedServiceDetails, ServiceDetails serviceDetails);
}
