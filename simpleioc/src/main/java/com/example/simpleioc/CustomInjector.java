package com.example.simpleioc;

import com.example.simpleioc.annotation.EntryPoint;
import com.example.simpleioc.config.CustomConfiguration;
import com.example.simpleioc.enums.DirectoryType;
import com.example.simpleioc.models.Directory;
import com.example.simpleioc.models.ServiceDetails;
import com.example.simpleioc.services.ClassLocator;
import com.example.simpleioc.services.ClassLocatorForDirectory;
import com.example.simpleioc.services.ClassLocatorForJarFile;
import com.example.simpleioc.services.DependencyContainer;
import com.example.simpleioc.services.DependencyContainerCached;
import com.example.simpleioc.services.DependencyResolveServiceImpl;
import com.example.simpleioc.services.DirectoryResolver;
import com.example.simpleioc.services.DirectoryResolverImpl;
import com.example.simpleioc.services.ObjectInstantiationService;
import com.example.simpleioc.services.ObjectInstantiationServiceImpl;
import com.example.simpleioc.services.ServicesInstantiationService;
import com.example.simpleioc.services.ServicesInstantiationServiceImpl;
import com.example.simpleioc.services.ServicesScanningService;
import com.example.simpleioc.services.ServicesScanningServiceImpl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomInjector {

    /**
     * Overload with default configuration.
     *
     * @param startupClass any class from the client side.
     */
    public static DependencyContainer run(Class<?> startupClass) {
        return run(startupClass, new CustomConfiguration());
    }

    /**
     * Runs with startup class.
     *
     * @param startupClass  any class from the client side.
     * @param configuration client configuration.
     */

    public static DependencyContainer run(Class<?> startupClass, CustomConfiguration configuration) {
        final DependencyContainer dependencyContainer = run(new File[]{
                new File(new DirectoryResolverImpl().resolveDirectory(startupClass).getDirectory()),
        }, configuration);

        runStartUpMethod(startupClass, dependencyContainer);

        return dependencyContainer;
    }

    public static DependencyContainer run(File[] startupDirectories, CustomConfiguration configuration) {
        final ServicesScanningService scanningService = new ServicesScanningServiceImpl(configuration.scanning());
        final ObjectInstantiationService objectInstantiationService = new ObjectInstantiationServiceImpl();
        final ServicesInstantiationService instantiationService = new ServicesInstantiationServiceImpl(
                configuration.instantiations(),
                objectInstantiationService,
                new DependencyResolveServiceImpl(configuration.instantiations())
        );

        final Set<Class<?>> locatedClasses = new HashSet<>();
        final List<ServiceDetails> serviceDetails = new ArrayList<>();

        final Thread runner = new Thread(() -> {
            locatedClasses.addAll(locateClasses(startupDirectories));
            final Set<ServiceDetails> mappedServices = new HashSet<>(scanningService.mapServices(locatedClasses));
            serviceDetails.addAll(new ArrayList<>(instantiationService.instantiateServices(mappedServices)));
        });

        runner.setContextClassLoader(configuration.scanning().getClassLoader());
        runner.start();
        try {
            runner.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }



        final DependencyContainer dependencyContainer = new DependencyContainerCached();
        dependencyContainer.init(locatedClasses, serviceDetails, objectInstantiationService);

        return dependencyContainer;
    }

    private static Set<Class<?>> locateClasses(File[] startupDirectories) {
        final Set<Class<?>> locatedClasses = new HashSet<>();
        final DirectoryResolver directoryResolver = new DirectoryResolverImpl();

        for (File startupDirectory : startupDirectories) {
            final Directory directory = directoryResolver.resolveDirectory(startupDirectory);

            ClassLocator classLocator = new ClassLocatorForDirectory();
            if (directory.getDirectoryType() == DirectoryType.JAR_FILE) {
                classLocator = new ClassLocatorForJarFile();
            }

            locatedClasses.addAll(classLocator.locateClasses(directory.getDirectory()));
        }

        return locatedClasses;
    }

    /**
     * This method calls executes when all services are loaded.
     * <p>
     * Looks for instantiated service from the given type.
     * <p>
     * If instance is found, looks for void method with 0 params
     * and with with @StartUp annotation and executes it.
     *
     * @param startupClass any class from the client side.
     */

    private static void runStartUpMethod(Class<?> startupClass, DependencyContainer dependencyContainer) {
        final ServiceDetails serviceDetails = dependencyContainer.getServiceDetails(startupClass, null);

        if (serviceDetails == null) {
            return;
        }

        for (Method declaredMethod : serviceDetails.getServiceType().getDeclaredMethods()) {
            if ((declaredMethod.getReturnType() != void.class &&
                    declaredMethod.getReturnType() != Void.class)
                    || !declaredMethod.isAnnotationPresent(EntryPoint.class)) {
                continue;
            }

            declaredMethod.setAccessible(true);
            final Object[] params = Arrays.stream(declaredMethod.getParameterTypes())
                    .map(dependencyContainer::getService)
                    .toArray(Object[]::new);

            try {
                declaredMethod.invoke(serviceDetails.getActualInstance(), params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return;
        }
    }
}
