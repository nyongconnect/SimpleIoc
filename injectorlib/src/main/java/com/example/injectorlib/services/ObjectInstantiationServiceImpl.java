package com.example.injectorlib.services;

import android.annotation.SuppressLint;

import com.example.injectorlib.annotation.Inject;
import com.example.injectorlib.exceptions.PreDestroyExecutionException;
import com.example.injectorlib.exceptions.ServiceInstantiationException;
import com.example.injectorlib.models.ServiceDetails;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * {@link ObjectInstantiationService} implementation.
 */
public class ObjectInstantiationServiceImpl implements ObjectInstantiationService {
    private static final String INVALID_PARAMETERS_COUNT_MSG = "Invalid parameters count for '%s'.";

    /**
     * Creates an instance for a service.
     * Invokes the PostConstruct method.
     *
     * @param serviceDetails    the given service details.
     */
    @SuppressLint("NewApi")
    @Override
    public void createInstance(ServiceDetails serviceDetails, Object[] constructorParams, Object[] injectFieldInstances) throws ServiceInstantiationException {
        final Constructor targetConstructor = serviceDetails.getTargetConstructor();

        if (constructorParams.length != targetConstructor.getParameterCount()) {
            throw new ServiceInstantiationException(String.format(INVALID_PARAMETERS_COUNT_MSG, serviceDetails.getServiceType().getName()));
        }

        try {
            final Object instance = targetConstructor.newInstance(constructorParams);
            serviceDetails.setInstance(instance);
            this.setInjectFieldInstances(serviceDetails, injectFieldInstances);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new ServiceInstantiationException(e.getMessage(), e);
        }
    }

    /**
     * Iterates all {@link Inject} annotated fields and sets them a given instance.
     *
     * @param serviceDetails - given service details.
     */
    private void setInjectFieldInstances(ServiceDetails serviceDetails, Object[] autowiredFieldInstances) throws IllegalAccessException {
        final Field[] injectAnnotatedFields = serviceDetails.getInjectAnnotatedFields();

        for (int i = 0; i < injectAnnotatedFields.length; i++) {
            injectAnnotatedFields[i].set(serviceDetails.getActualInstance(), autowiredFieldInstances[i]);
        }
    }

    /**
     * Sets the instance to null.
     * Invokes post construct method for the given service details if one is present.
     *
     * @param serviceDetails given service details.
     */
    @Override
    public void destroyInstance(ServiceDetails serviceDetails) throws PreDestroyExecutionException {

        serviceDetails.setInstance(null);
    }
}
