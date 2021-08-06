package com.example.simpleioc.services;


import com.example.simpleioc.enums.DirectoryType;
import com.example.simpleioc.models.Directory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;

public class DirectoryResolverImpl implements DirectoryResolver {

    private static final String JAR_FILE_EXTENSION = ".jar";

    @Override
    public Directory resolveDirectory(Class<?> startupClass) {
        final String directory = this.getDirectory(startupClass);

        return new Directory(directory, this.getDirectoryType(directory));
    }

    @Override
    public Directory resolveDirectory(File directory) {
        try {
            return new Directory(directory.getCanonicalPath(), this.getDirectoryType(directory.getCanonicalPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the root dir where the given class resides.
     *
     * @param cls - the given class.
     */
    private String getDirectory(Class<?> cls) {
        try {

            ProtectionDomain protectionDomain = cls.getProtectionDomain();
            return URLDecoder.decode(protectionDomain.getCodeSource().getLocation().getFile(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param directory given directory.
     * @return JAR_FILE or DIRECTORY.
     */
    private DirectoryType getDirectoryType(String directory) {
        final File file = new File(directory);

        if (!file.isDirectory() && directory.endsWith(JAR_FILE_EXTENSION)) {
            return DirectoryType.JAR_FILE;
        }

        return DirectoryType.DIRECTORY;
    }
}
