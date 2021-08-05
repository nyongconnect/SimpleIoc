package com.example.injectorlib.services;

import com.example.injectorlib.exceptions.ClassLocationException;

import java.util.Set;

public interface ClassLocator {

    Set<Class<?>> locateClasses(String directory) throws ClassLocationException;
}
