package com.example.injectorlib.services;


import com.example.injectorlib.models.ServiceDetails;

import java.util.Set;

public interface ServicesScanningService {

    Set<ServiceDetails> mapServices(Set<Class<?>> locatedClasses);
}
