package com.example.injectorlib.utils;


import android.annotation.SuppressLint;

import com.example.injectorlib.models.ServiceDetails;

import java.util.Comparator;

/**
 * Comparator for @ServiceDetails.
 * <p>
 * Used to compare the number of the constructor parameters for each ServiceDetails' target constructor.
 */
public class ServiceDetailsConstructComparator implements Comparator<ServiceDetails> {
    @SuppressLint("NewApi")
    @Override
    public int compare(ServiceDetails serviceDetails1, ServiceDetails serviceDetails2) {
        if (serviceDetails1.getTargetConstructor() == null || serviceDetails2.getTargetConstructor() == null) {
            return 0;
        }

        return Integer.compare(
                serviceDetails1.getTargetConstructor().getParameterCount(),
                serviceDetails2.getTargetConstructor().getParameterCount()
        );
    }
}
