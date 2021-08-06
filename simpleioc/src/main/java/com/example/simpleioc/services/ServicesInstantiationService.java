package com.example.simpleioc.services;

import com.example.simpleioc.exceptions.ServiceInstantiationException;
import com.example.simpleioc.models.ServiceDetails;

import java.util.Collection;
import java.util.Set;

public interface ServicesInstantiationService {
    Collection<ServiceDetails> instantiateServices(Set<ServiceDetails> mappedServices) throws ServiceInstantiationException;
}
