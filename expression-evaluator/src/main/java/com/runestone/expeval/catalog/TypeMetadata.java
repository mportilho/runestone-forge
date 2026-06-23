package com.runestone.expeval.catalog;

import java.util.Map;
import java.util.Objects;

public record TypeMetadata(
        Class<?> javaClass,
        Map<String, PropertyDescriptor> properties,
        Map<String, java.util.List<MethodDescriptor>> methods
) {

    public TypeMetadata {
        Objects.requireNonNull(javaClass, "javaClass must not be null");
        properties = Map.copyOf(Objects.requireNonNull(properties, "properties must not be null"));
        methods = copyMethods(methods);
    }

    private static Map<String, java.util.List<MethodDescriptor>> copyMethods(
            Map<String, java.util.List<MethodDescriptor>> methods) {
        Objects.requireNonNull(methods, "methods must not be null");
        Map<String, java.util.List<MethodDescriptor>> copied = new java.util.LinkedHashMap<>();
        methods.forEach((name, descriptors) -> copied.put(
                name,
                java.util.List.copyOf(Objects.requireNonNull(descriptors, "descriptors must not be null"))
        ));
        return Map.copyOf(copied);
    }
}
