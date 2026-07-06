package com.runestone.expeval_mk3.api;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Catalog of Java-backed Tipo Objeto metadata available to navigation resolution.
 */
public final class JavaTypeCatalog {

    private static final JavaTypeCatalog EMPTY = new JavaTypeCatalog(Map.of());
    private static final Comparator<RegisteredJavaType> REGISTERED_TYPE_ORDER = Comparator
            .comparing((RegisteredJavaType type) -> type.objectType().name())
            .thenComparing(type -> type.javaClass().getName());
    private static final Comparator<Method> METHOD_ORDER = Comparator
            .comparing(Method::getName)
            .thenComparing(JavaTypeCatalog::canonicalMethodParameters)
            .thenComparing(method -> method.getDeclaringClass().getName())
            .thenComparing(method -> method.getReturnType().getName());
    private static final Comparator<FunctionSignature> SIGNATURE_ORDER = Comparator
            .comparing(FunctionSignature::languageName)
            .thenComparing(JavaTypeCatalog::canonicalSignatureParameters);

    private final Map<ObjectType, RegisteredJavaType> typesByObjectType;
    private final List<RegisteredJavaType> types;

    private JavaTypeCatalog(Map<ObjectType, RegisteredJavaType> source) {
        ArrayList<RegisteredJavaType> sortedTypes = new ArrayList<>(source.values());
        sortedTypes.sort(REGISTERED_TYPE_ORDER);

        LinkedHashMap<ObjectType, RegisteredJavaType> byObjectType = new LinkedHashMap<>();
        for (RegisteredJavaType registeredJavaType : sortedTypes) {
            byObjectType.put(registeredJavaType.objectType(), registeredJavaType);
        }

        typesByObjectType = Collections.unmodifiableMap(byObjectType);
        types = List.copyOf(sortedTypes);
    }

    public static JavaTypeCatalog empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RegisteredJavaType.Builder registerJavaType(Class<?> javaClass, String objectTypeName) {
        return registerJavaType(javaClass, new ObjectType(objectTypeName));
    }

    public static RegisteredJavaType.Builder registerJavaType(Class<?> javaClass, ObjectType objectType) {
        return RegisteredJavaType.builder(javaClass, objectType);
    }

    public Optional<RegisteredJavaType> find(ObjectType objectType) {
        return Optional.ofNullable(typesByObjectType.get(Objects.requireNonNull(objectType, "objectType")));
    }

    public Collection<RegisteredJavaType> types() {
        return types;
    }

    public Map<ObjectType, RegisteredJavaType> asMap() {
        return typesByObjectType;
    }

    public int size() {
        return types.size();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JavaTypeCatalog that)) {
            return false;
        }
        return typesByObjectType.equals(that.typesByObjectType);
    }

    @Override
    public int hashCode() {
        return typesByObjectType.hashCode();
    }

    @Override
    public String toString() {
        return "JavaTypeCatalog[size=" + types.size() + ']';
    }

    private static Map<String, PropertyMember> discoverProperties(Class<?> javaClass) {
        LinkedHashMap<String, PropertyMember> properties = new LinkedHashMap<>();
        if (javaClass.isRecord()) {
            RecordComponent[] recordComponents = javaClass.getRecordComponents();
            for (RecordComponent recordComponent : recordComponents) {
                Method accessor = recordComponent.getAccessor();
                addProperty(properties, new PropertyMember(
                        recordComponent.getName(),
                        accessor,
                        inferReturnType(accessor.getReturnType())));
            }
        }

        ArrayList<Method> methods = new ArrayList<>(List.of(javaClass.getMethods()));
        methods.sort(METHOD_ORDER);
        for (Method method : methods) {
            propertyName(method).ifPresent(propertyName -> addProperty(properties, new PropertyMember(
                    propertyName,
                    method,
                    inferReturnType(method.getReturnType()))));
        }
        return properties;
    }

    private static Optional<String> propertyName(Method method) {
        if (method.getParameterCount() != 0
                || method.getReturnType() == Void.TYPE
                || Modifier.isStatic(method.getModifiers())
                || method.isBridge()
                || method.isSynthetic()
                || isObjectMethod(method)) {
            return Optional.empty();
        }
        String methodName = method.getName();
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Optional.of(Introspector.decapitalize(methodName.substring(3)));
        }
        if (methodName.startsWith("is")
                && methodName.length() > 2
                && (method.getReturnType() == Boolean.TYPE || method.getReturnType() == Boolean.class)) {
            return Optional.of(Introspector.decapitalize(methodName.substring(2)));
        }
        return Optional.empty();
    }

    private static void addProperty(Map<String, PropertyMember> properties, PropertyMember property) {
        PropertyMember previous = properties.putIfAbsent(property.name(), property);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate property '" + property.name() + "' for Java type "
                    + previous.accessor().getDeclaringClass().getName());
        }
    }

    private static List<MethodMember> discoverPublicMethods(Class<?> javaClass) {
        ArrayList<Method> methods = new ArrayList<>(List.of(javaClass.getMethods()));
        methods.sort(METHOD_ORDER);

        ArrayList<MethodMember> members = new ArrayList<>();
        for (Method method : methods) {
            methodFromPublicDiscovery(method).ifPresent(members::add);
        }
        return members;
    }

    private static Optional<MethodMember> methodFromPublicDiscovery(Method method) {
        if (Modifier.isStatic(method.getModifiers())
                || method.getReturnType() == Void.TYPE
                || method.isBridge()
                || method.isSynthetic()
                || method.isVarArgs()
                || isObjectMethod(method)) {
            return Optional.empty();
        }

        ArrayList<ExpressionType> parameterTypes = new ArrayList<>(method.getParameterCount());
        for (Class<?> parameterType : method.getParameterTypes()) {
            Optional<ExpressionType> expressionType = inferSupportedParameterType(parameterType);
            if (expressionType.isEmpty()) {
                return Optional.empty();
            }
            parameterTypes.add(expressionType.orElseThrow());
        }

        return Optional.of(new MethodMember(
                FunctionSignature.of(method.getName(), parameterTypes),
                method,
                inferReturnType(method.getReturnType()),
                false));
    }

    private static void addMethod(Map<FunctionSignature, MethodMember> methods, MethodMember method) {
        MethodMember previous = methods.putIfAbsent(method.signature(), method);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate member function signature '"
                    + method.signature().languageName()
                    + "' for Java type "
                    + method.method().getDeclaringClass().getName());
        }
    }

    private static ExpressionType inferReturnType(Class<?> javaType) {
        return inferSupportedParameterType(javaType).orElse(UnknownType.INSTANCE);
    }

    private static Optional<ExpressionType> inferSupportedParameterType(Class<?> javaType) {
        Objects.requireNonNull(javaType, "javaType");
        if (javaType == BigDecimal.class
                || javaType == BigInteger.class
                || javaType == Byte.class
                || javaType == Short.class
                || javaType == Integer.class
                || javaType == Long.class
                || javaType == Float.class
                || javaType == Double.class
                || javaType == byte.class
                || javaType == short.class
                || javaType == int.class
                || javaType == long.class
                || javaType == float.class
                || javaType == double.class) {
            return Optional.of(ScalarType.NUMBER);
        }
        if (javaType == Boolean.class || javaType == boolean.class) {
            return Optional.of(ScalarType.BOOLEAN);
        }
        if (javaType == String.class || javaType == Character.class || javaType == char.class) {
            return Optional.of(ScalarType.STRING);
        }
        if (javaType == LocalDate.class) {
            return Optional.of(ScalarType.DATE);
        }
        if (javaType == LocalTime.class) {
            return Optional.of(ScalarType.TIME);
        }
        if (javaType == LocalDateTime.class || javaType == OffsetDateTime.class || javaType == ZonedDateTime.class) {
            return Optional.of(ScalarType.DATETIME);
        }
        return Optional.empty();
    }

    private static boolean isObjectMethod(Method method) {
        for (Method objectMethod : Object.class.getMethods()) {
            if (objectMethod.getName().equals(method.getName())
                    && List.of(objectMethod.getParameterTypes()).equals(List.of(method.getParameterTypes()))) {
                return true;
            }
        }
        return false;
    }

    private static String canonicalMethodParameters(Method method) {
        StringBuilder canonical = new StringBuilder();
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (!canonical.isEmpty()) {
                canonical.append(',');
            }
            canonical.append(parameterType.getName());
        }
        return canonical.toString();
    }

    private static String canonicalSignatureParameters(FunctionSignature signature) {
        StringBuilder canonical = new StringBuilder();
        for (ExpressionType parameterType : signature.parameterTypes()) {
            if (!canonical.isEmpty()) {
                canonical.append(',');
            }
            canonical.append(ExpressionTypes.canonical(parameterType));
        }
        return canonical.toString();
    }

    static String canonicalMethod(Method method) {
        return method.getDeclaringClass().getName() + '.' + method.getName() + '(' + canonicalMethodParameters(method) + ')';
    }

    static List<RegisteredJavaType> canonicalTypes(JavaTypeCatalog catalog) {
        return catalog.types;
    }

    static List<PropertyMember> canonicalProperties(RegisteredJavaType type) {
        ArrayList<PropertyMember> properties = new ArrayList<>(type.properties().values());
        properties.sort(Comparator.comparing(PropertyMember::name));
        return properties;
    }

    static List<MethodMember> canonicalMethods(RegisteredJavaType type) {
        ArrayList<MethodMember> methods = new ArrayList<>(type.methods().values());
        methods.sort(Comparator.comparing(MethodMember::signature, SIGNATURE_ORDER));
        return methods;
    }

    public static final class Builder {

        private final Map<ObjectType, RegisteredJavaType> registrations = new HashMap<>();

        private Builder() {
        }

        private Builder(JavaTypeCatalog catalog) {
            registrations.putAll(catalog.typesByObjectType);
        }

        public Builder registerJavaType(RegisteredJavaType registeredJavaType) {
            Objects.requireNonNull(registeredJavaType, "registeredJavaType");
            RegisteredJavaType previous = registrations.putIfAbsent(registeredJavaType.objectType(), registeredJavaType);
            if (previous != null) {
                throw new IllegalArgumentException("Java type '" + registeredJavaType.objectType().name()
                        + "' is already registered");
            }
            return this;
        }

        public JavaTypeCatalog build() {
            if (registrations.isEmpty()) {
                return EMPTY;
            }
            return new JavaTypeCatalog(registrations);
        }
    }

    public record RegisteredJavaType(
            Class<?> javaClass,
            ObjectType objectType,
            Map<String, PropertyMember> properties,
            Map<FunctionSignature, MethodMember> methods) {

        public RegisteredJavaType {
            javaClass = Objects.requireNonNull(javaClass, "javaClass");
            objectType = Objects.requireNonNull(objectType, "objectType");
            properties = copyProperties(properties);
            methods = copyMethods(methods);
            validateMembersBelongToJavaType(javaClass, properties, methods);
        }

        public static Builder builder(Class<?> javaClass, ObjectType objectType) {
            return new Builder(javaClass, objectType);
        }

        public static final class Builder {

            private final Class<?> javaClass;
            private final ObjectType objectType;
            private final List<PropertyMember> explicitProperties = new ArrayList<>();
            private final List<MethodMember> explicitMethods = new ArrayList<>();
            private boolean includeProperties = true;
            private boolean includePublicMethods;

            private Builder(Class<?> javaClass, ObjectType objectType) {
                this.javaClass = Objects.requireNonNull(javaClass, "javaClass");
                this.objectType = Objects.requireNonNull(objectType, "objectType");
            }

            public Builder includePublicMethods() {
                includePublicMethods = true;
                return this;
            }

            public Builder withoutProperties() {
                includeProperties = false;
                return this;
            }

            public Builder property(String name, Method accessor, ExpressionType returnType) {
                validateMethodBelongsToType(accessor);
                explicitProperties.add(new PropertyMember(name, accessor, returnType));
                return this;
            }

            public Builder method(
                    String languageName,
                    Method method,
                    ExpressionType returnType,
                    List<ExpressionType> parameterTypes) {
                parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes"));
                validateMethodBelongsToType(method);
                if (parameterTypes.size() != method.getParameterCount()) {
                    throw new IllegalArgumentException("method signature arity must match reflected method arity");
                }
                explicitMethods.add(new MethodMember(
                        FunctionSignature.of(languageName, parameterTypes),
                        method,
                        Objects.requireNonNull(returnType, "returnType"),
                        true));
                return this;
            }

            public RegisteredJavaType build() {
                LinkedHashMap<String, PropertyMember> properties = new LinkedHashMap<>();
                for (PropertyMember explicitProperty : explicitProperties) {
                    addProperty(properties, explicitProperty);
                }
                if (includeProperties) {
                    for (PropertyMember discoveredProperty : discoverProperties(javaClass).values()) {
                        addProperty(properties, discoveredProperty);
                    }
                }
                LinkedHashMap<FunctionSignature, MethodMember> methods = new LinkedHashMap<>();
                for (MethodMember explicitMethod : explicitMethods) {
                    addMethod(methods, explicitMethod);
                }
                if (includePublicMethods) {
                    for (MethodMember discoveredMethod : discoverPublicMethods(javaClass)) {
                        addMethod(methods, discoveredMethod);
                    }
                }
                return new RegisteredJavaType(javaClass, objectType, properties, methods);
            }

            private void validateMethodBelongsToType(Method method) {
                Objects.requireNonNull(method, "method");
                validateMemberBelongsToJavaType(javaClass, method);
            }
        }
    }

    public record PropertyMember(String name, Method accessor, ExpressionType returnType) {

        public PropertyMember {
            name = FunctionSignature.validateLanguageName(name);
            accessor = Objects.requireNonNull(accessor, "accessor");
            returnType = Objects.requireNonNull(returnType, "returnType");
            if (accessor.getParameterCount() != 0) {
                throw new IllegalArgumentException("property accessor must not declare parameters");
            }
            if (accessor.getReturnType() == Void.TYPE) {
                throw new IllegalArgumentException("property accessor must return a value");
            }
            if (Modifier.isStatic(accessor.getModifiers())) {
                throw new IllegalArgumentException("property accessor must not be static");
            }
            if (accessor.isBridge() || accessor.isSynthetic()) {
                throw new IllegalArgumentException("property accessor must not be bridge or synthetic");
            }
        }
    }

    public record MethodMember(
            FunctionSignature signature,
            Method method,
            ExpressionType returnType,
            boolean explicitlyRegistered) {

        public MethodMember {
            signature = Objects.requireNonNull(signature, "signature");
            method = Objects.requireNonNull(method, "method");
            returnType = Objects.requireNonNull(returnType, "returnType");
            if (signature.arity() != method.getParameterCount()) {
                throw new IllegalArgumentException("method signature arity must match reflected method arity");
            }
        }
    }

    private static Map<String, PropertyMember> copyProperties(Map<String, PropertyMember> source) {
        Objects.requireNonNull(source, "properties");
        ArrayList<PropertyMember> sortedProperties = new ArrayList<>(source.size());
        LinkedHashMap<String, PropertyMember> copy = new LinkedHashMap<>();
        for (Map.Entry<String, PropertyMember> entry : source.entrySet()) {
            String propertyName = Objects.requireNonNull(entry.getKey(), "propertyName");
            PropertyMember property = Objects.requireNonNull(entry.getValue(), "property");
            if (!propertyName.equals(property.name())) {
                throw new IllegalArgumentException("property key must match property name");
            }
            sortedProperties.add(property);
        }
        sortedProperties.sort(Comparator.comparing(PropertyMember::name));
        for (PropertyMember property : sortedProperties) {
            PropertyMember previous = copy.putIfAbsent(property.name(), property);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate property '" + property.name() + "'");
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    private static void validateMembersBelongToJavaType(
            Class<?> javaClass,
            Map<String, PropertyMember> properties,
            Map<FunctionSignature, MethodMember> methods) {
        for (PropertyMember property : properties.values()) {
            validateMemberBelongsToJavaType(javaClass, property.accessor());
        }
        for (MethodMember method : methods.values()) {
            validateMemberBelongsToJavaType(javaClass, method.method());
        }
    }

    private static void validateMemberBelongsToJavaType(Class<?> javaClass, Method method) {
        if (!method.getDeclaringClass().isAssignableFrom(javaClass)) {
            throw new IllegalArgumentException("member " + method + " is not available on Java type "
                    + javaClass.getName());
        }
    }

    private static Map<FunctionSignature, MethodMember> copyMethods(Map<FunctionSignature, MethodMember> source) {
        Objects.requireNonNull(source, "methods");
        ArrayList<MethodMember> sortedMethods = new ArrayList<>(source.size());
        LinkedHashMap<FunctionSignature, MethodMember> copy = new LinkedHashMap<>();
        for (Map.Entry<FunctionSignature, MethodMember> entry : source.entrySet()) {
            FunctionSignature methodSignature = Objects.requireNonNull(entry.getKey(), "methodSignature");
            MethodMember method = Objects.requireNonNull(entry.getValue(), "method");
            if (!methodSignature.equals(method.signature())) {
                throw new IllegalArgumentException("method key must match method signature");
            }
            sortedMethods.add(method);
        }
        sortedMethods.sort(Comparator.comparing(MethodMember::signature, SIGNATURE_ORDER));
        for (MethodMember method : sortedMethods) {
            MethodMember previous = copy.putIfAbsent(method.signature(), method);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate member function signature '"
                        + method.signature().languageName() + "'");
            }
        }
        return Collections.unmodifiableMap(copy);
    }
}
