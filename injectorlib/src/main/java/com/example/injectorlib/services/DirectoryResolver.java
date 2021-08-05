package com.example.injectorlib.services;

import com.example.injectorlib.models.Directory;

import java.io.File;

public interface DirectoryResolver {

    Directory resolveDirectory(Class<?> startupClass);

    Directory resolveDirectory(File directory);

}
