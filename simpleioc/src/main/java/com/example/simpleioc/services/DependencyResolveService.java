package com.example.simpleioc.services;

import com.example.simpleioc.exceptions.ServiceInstantiationException;
import com.example.simpleioc.models.EnqueuedServiceDetails;
import com.example.simpleioc.models.ServiceDetails;

import java.util.Collection;

public interface DependencyResolveService {

    void init(Collection<ServiceDetails> mappedServices);

    void checkDependencies(Collection<EnqueuedServiceDetails> enqueuedServiceDetails) throws ServiceInstantiationException;

    void addDependency(EnqueuedServiceDetails enqueuedServiceDetails, ServiceDetails serviceDetails);

    boolean isServiceResolved(EnqueuedServiceDetails enqueuedServiceDetails);

    boolean isDependencyRequired(EnqueuedServiceDetails enqueuedServiceDetails, ServiceDetails serviceDetails);
}
