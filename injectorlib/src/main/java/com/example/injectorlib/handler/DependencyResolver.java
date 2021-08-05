package com.example.injectorlib.handler;


import com.example.injectorlib.models.DependencyParam;

public interface DependencyResolver {

    boolean canResolve(DependencyParam dependencyParam);

    Object resolve(DependencyParam dependencyParam);
}
