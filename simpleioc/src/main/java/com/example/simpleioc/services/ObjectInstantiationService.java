package com.example.simpleioc.services;

import com.example.simpleioc.exceptions.PreDestroyExecutionException;
import com.example.simpleioc.exceptions.ServiceInstantiationException;
import com.example.simpleioc.models.ServiceDetails;

public interface ObjectInstantiationService {

    void createInstance(ServiceDetails serviceDetails, Object[] constructorParams, Object[] autowiredFieldInstances) throws ServiceInstantiationException;

    void destroyInstance(ServiceDetails serviceDetails) throws PreDestroyExecutionException;
}
