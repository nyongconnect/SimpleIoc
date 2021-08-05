package com.example.injectorlib.services;


import com.example.injectorlib.exceptions.ServiceInstantiationException;
import com.example.injectorlib.models.ServiceDetails;

import java.util.Collection;
import java.util.Set;

public interface ServicesInstantiationService {
    Collection<ServiceDetails> instantiateServices(Set<ServiceDetails> mappedServices) throws ServiceInstantiationException;
}
