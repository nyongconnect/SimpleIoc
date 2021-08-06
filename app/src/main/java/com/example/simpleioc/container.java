package com.example.simpleioc;

import com.example.simpleioc.annotation.Component;
import com.example.simpleioc.annotation.EntryPoint;
import com.example.simpleioc.annotation.Inject;
import com.example.simpleioc.services.DependencyContainer;

@Component
public class container {

    @Inject
    private TestInterface hou;

    @EntryPoint
    void ma(DependencyContainer dependencyContainer) {
        hou.eat();
    }
}
