package com.example.injectorlib.services;


import com.example.injectorlib.exceptions.PreDestroyExecutionException;
import com.example.injectorlib.exceptions.ServiceInstantiationException;
import com.example.injectorlib.models.ServiceDetails;

public interface ObjectInstantiationService {

    void createInstance(ServiceDetails serviceDetails, Object[] constructorParams, Object[] autowiredFieldInstances) throws ServiceInstantiationException;

    void destroyInstance(ServiceDetails serviceDetails) throws PreDestroyExecutionException;
}
