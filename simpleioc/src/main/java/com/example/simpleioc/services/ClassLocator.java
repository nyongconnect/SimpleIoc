package com.example.simpleioc.services;

import com.example.simpleioc.exceptions.ClassLocationException;

import java.util.Set;

public interface ClassLocator {

    Set<Class<?>> locateClasses(String directory) throws ClassLocationException;
}
