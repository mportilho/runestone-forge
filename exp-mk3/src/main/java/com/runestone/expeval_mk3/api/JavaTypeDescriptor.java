package com.runestone.expeval_mk3.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final List<JavaWildcardChildDescriptor> wildcardChildren;
    private final ExpressionType wildcardChildType;

    JavaTypeDescriptor(
            Class<?> javaType,
            ObjectType objectType,
            Map<String, JavaPropertyDescriptor> properties,
            Map<FunctionSignature, JavaMethodDescriptor> methods,
            List<JavaWildcardChildDescriptor> wildcardChildren) {
        this.javaType = Objects.requireNonNull(javaType, "javaType");
        this.objectType = Objects.requireNonNull(objectType, "objectType");
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(new TreeMap<>(properties)));
        this.methods = Collections.unmodifiableNavigableMap(new TreeMap<>(methods));
        this.wildcardChildren = List.copyOf(wildcardChildren);
        this.wildcardChildType = commonWildcardChildType(this.wildcardChildren);
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

    public List<JavaWildcardChildDescriptor> wildcardChildren() {
        return wildcardChildren;
    }

    public Optional<ExpressionType> wildcardChildType() {
        return Optional.ofNullable(wildcardChildType);
    }

    public int propertyCount() {
        return properties.size();
    }

    public int methodCount() {
        return methods.size();
    }

    private static ExpressionType commonWildcardChildType(List<JavaWildcardChildDescriptor> children) {
        if (children.isEmpty()) {
            return null;
        }
        ExpressionType commonType = children.getFirst().type();
        for (int index = 1; index < children.size(); index++) {
            JavaWildcardChildDescriptor child = children.get(index);
            if (!commonType.equals(child.type())) {
                throw new IllegalArgumentException("wildcard child members must have one expression type: "
                        + commonType + " and " + child.type());
            }
        }
        return commonType;
    }
}
