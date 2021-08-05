package com.example.injectorlib.event;


import com.example.injectorlib.models.ServiceDetails;

@FunctionalInterface
public interface ServiceDetailsCreated {
    void serviceDetailsCreated(ServiceDetails serviceDetails);
}
