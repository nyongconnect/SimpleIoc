package com.example.simpleioc.services;


import com.example.simpleioc.models.Directory;

import java.io.File;

public interface DirectoryResolver {

    Directory resolveDirectory(Class<?> startupClass);

    Directory resolveDirectory(File directory);

}
