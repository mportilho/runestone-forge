package com.runestone.expeval_mk3.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Immutable metadata for one registered Tipo Java Registrado.
 */
public final class JavaTypeDescriptor {

    private final Class<?> javaType;
    private final ObjectType objectType;
    private final Map<String, JavaPropertyDescriptor> properties;
    private final NavigableMap<FunctionSignature, JavaMethodDescriptor> methods;

    JavaTypeDescriptor(
            Class<?> javaType,
            ObjectType objectType,
            Map<String, JavaPropertyDescriptor> properties,
            Map<FunctionSignature, JavaMethodDescriptor> methods) {
        this.javaType = Objects.requireNonNull(javaType, "javaType");
        this.objectType = Objects.requireNonNull(objectType, "objectType");
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(new TreeMap<>(properties)));
        this.methods = Collections.unmodifiableNavigableMap(new TreeMap<>(methods));
    }

    public Class<?> javaType() {
        return javaType;
    }

    public ObjectType objectType() {
        return objectType;
    }

    public Map<String, JavaPropertyDescriptor> properties() {
        return properties;
    }

    public Collection<JavaMethodDescriptor> methods() {
        return methods.values();
    }

    public Optional<JavaPropertyDescriptor> findProperty(String name) {
        return Optional.ofNullable(properties.get(FunctionSignature.validateLanguageName(name)));
    }

    public Optional<JavaMethodDescriptor> findMethod(FunctionSignature signature) {
        return Optional.ofNullable(methods.get(Objects.requireNonNull(signature, "signature")));
    }

    public int propertyCount() {
        return properties.size();
    }

    public int methodCount() {
        return methods.size();
    }
}
