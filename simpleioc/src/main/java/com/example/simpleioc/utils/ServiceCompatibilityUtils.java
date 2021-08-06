package com.example.simpleioc.utils;

import com.example.simpleioc.models.ServiceDetails;

public final class ServiceCompatibilityUtils {

    public static boolean isServiceCompatible(ServiceDetails serviceDetails, Class<?> requiredType, String instanceName) {
        // and (instanceName if not null equals service's instance name)

        final boolean isRequiredTypeAssignable = requiredType.isAssignableFrom(serviceDetails.getServiceType());
        final boolean isRequiredTypeAssignable2 = serviceDetails.getInstance() != null &&
                requiredType.isAssignableFrom(serviceDetails.getInstance().getClass());

        final boolean instanceNameMatches = instanceName == null ||
                instanceName.equalsIgnoreCase(serviceDetails.getInstanceName());

        return (isRequiredTypeAssignable || isRequiredTypeAssignable2) && instanceNameMatches;
    }
}
