package com.example.simpleioc.services;

import com.example.simpleioc.models.ServiceDetails;

import java.util.Set;

public interface ServicesScanningService {

    Set<ServiceDetails> mapServices(Set<Class<?>> locatedClasses);
}
