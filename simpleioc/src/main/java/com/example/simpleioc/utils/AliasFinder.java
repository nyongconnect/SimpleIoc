package com.example.simpleioc.utils;

import java.lang.annotation.Annotation;

public class AliasFinder {

    public static Annotation getAnnotation(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == requiredAnnotation) {
                return annotation;
            }
        }
        return null;
    }

    public static boolean isAnnotationPresent(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation) {
        return getAnnotation(annotations, requiredAnnotation) != null;
    }
}
