package com.asv.unapi.service.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author alexandrov
 * @since 20.06.2016
 */
public class Assert {

    public static void assertAnnotationPresent(Field field, Class<? extends Annotation> annotationClass) {
        if (!field.isAnnotationPresent(annotationClass)) {
            throw new RuntimeException(String.format("Annotation %s is not present in the field %s", annotationClass.getName(), field.getName()));
        }
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }

    public static void mustHaveValues(Map<?, ?> col, String message) {
        if (col.size() == 0) {
            throw new RuntimeException(message);
        }
    }

}