package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Immutable catalog of registered Java-backed Tipo Objeto metadata.
 */
public final class JavaTypeCatalog {

    private static final JavaTypeCatalog EMPTY = new JavaTypeCatalog(Map.of());
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle COLLECTION_TO_ARRAY = findCollectionToArray();
    private static final MethodHandle SEQUENCE_ARGUMENT = findSequenceArgument();
    private static final MethodHandle SEQUENCE_RESULT = findSequenceResult();

    private final Map<Class<?>, JavaTypeDescriptor> descriptorsByClass;
    private final Map<ObjectType, JavaTypeDescriptor> descriptorsByObjectType;

    private JavaTypeCatalog(Map<Class<?>, JavaTypeDescriptor> descriptorsByClass) {
        TreeMap<String, JavaTypeDescriptor> sorted = new TreeMap<>();
        for (JavaTypeDescriptor descriptor : descriptorsByClass.values()) {
            sorted.put(descriptor.javaType().getName(), descriptor);
        }
        Map<Class<?>, JavaTypeDescriptor> byClass = new LinkedHashMap<>();
        Map<ObjectType, JavaTypeDescriptor> byObjectType = new LinkedHashMap<>();
        for (JavaTypeDescriptor descriptor : sorted.values()) {
            byClass.put(descriptor.javaType(), descriptor);
            byObjectType.put(descriptor.objectType(), descriptor);
        }
        this.descriptorsByClass = Collections.unmodifiableMap(byClass);
        descriptorsByObjectType = Collections.unmodifiableMap(byObjectType);
    }

    public static JavaTypeCatalog empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the fixed non-null contract for all registered Java member results.
     */
    public static RuntimeNullability registeredMemberReturnNullability() {
        return JavaMemberTypes.returnNullability();
    }

    public Optional<JavaTypeDescriptor> find(Class<?> javaType) {
        return Optional.ofNullable(descriptorsByClass.get(Objects.requireNonNull(javaType, "javaType")));
    }

    public Optional<JavaTypeDescriptor> find(ObjectType objectType) {
        return Optional.ofNullable(descriptorsByObjectType.get(Objects.requireNonNull(objectType, "objectType")));
    }

    public Collection<JavaTypeDescriptor> values() {
        return descriptorsByClass.values();
    }

    public int size() {
        return descriptorsByClass.size();
    }

    @Override
    public String toString() {
        return "JavaTypeCatalog[size=" + descriptorsByClass.size() + ']';
    }

    public static final class Builder {

        private final Map<Class<?>, JavaTypeRegistration> registrations = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder registerJavaType(Class<?> javaType) {
            registration(javaType);
            return this;
        }

        public Builder registerJavaTypeWithPublicMethods(Class<?> javaType) {
            registration(javaType).includePublicMethods();
            return this;
        }

        public Builder registerJavaTypeMethod(Class<?> javaType, String methodName, Class<?>... javaParameterTypes) {
            registration(javaType).addMethod(methodName, javaParameterTypes);
            return this;
        }

        public JavaTypeCatalog build() {
            if (registrations.isEmpty()) {
                return EMPTY;
            }
            Map<Class<?>, JavaTypeDescriptor> descriptors = new LinkedHashMap<>();
            for (JavaTypeRegistration registration : registrations.values()) {
                JavaTypeDescriptor descriptor = registration.toDescriptor();
                JavaTypeDescriptor previous = descriptors.putIfAbsent(descriptor.javaType(), descriptor);
                if (previous != null) {
                    throw new IllegalArgumentException("Java type already registered: " + descriptor.javaType().getName());
                }
            }
            return new JavaTypeCatalog(descriptors);
        }

        private JavaTypeRegistration registration(Class<?> javaType) {
            Objects.requireNonNull(javaType, "javaType");
            return registrations.computeIfAbsent(javaType, JavaTypeRegistration::new);
        }
    }

    private static final class JavaTypeRegistration {

        private final Class<?> javaType;
        private final List<MethodKey> explicitMethods = new ArrayList<>();
        private boolean includePublicMethods;

        private JavaTypeRegistration(Class<?> javaType) {
            this.javaType = Objects.requireNonNull(javaType, "javaType");
        }

        private void includePublicMethods() {
            includePublicMethods = true;
        }

        private void addMethod(String methodName, Class<?>... javaParameterTypes) {
            explicitMethods.add(new MethodKey(methodName, javaParameterTypes));
        }

        private JavaTypeDescriptor toDescriptor() {
            return new JavaTypeDescriptor(
                    javaType,
                    new ObjectType(javaType.getName()),
                    discoverProperties(javaType),
                    discoverMethods());
        }

        private Map<FunctionSignature, JavaMethodDescriptor> discoverMethods() {
            List<Method> selectedMethods = new ArrayList<>();
            Set<Method> selectedMethodSet = new LinkedHashSet<>();
            for (MethodKey explicitMethod : explicitMethods) {
                Method method = explicitMethod.resolve(javaType);
                if (selectedMethodSet.add(method)) {
                    selectedMethods.add(method);
                }
            }
            if (includePublicMethods) {
                for (Method method : sortedPublicMethods(javaType)) {
                    if (!isPublicMethodCandidate(method)) {
                        continue;
                    }
                    if (isSupportedPublicMethod(method) && selectedMethodSet.add(method)) {
                        selectedMethods.add(method);
                    }
                }
            }
            selectedMethods.sort(JavaTypeCatalog::compareMethods);

            Map<FunctionSignature, JavaMethodDescriptor> descriptors = new LinkedHashMap<>();
            for (Method method : selectedMethods) {
                JavaMethodDescriptor descriptor = createMethodDescriptor(javaType, method, method.isVarArgs());
                JavaMethodDescriptor previous = descriptors.putIfAbsent(descriptor.signature(), descriptor);
                if (previous != null) {
                    throw new IllegalArgumentException("duplicate Java member method signature: "
                            + descriptor.signature().canonical());
                }
            }
            return descriptors;
        }
    }

    private static Map<String, JavaPropertyDescriptor> discoverProperties(Class<?> javaType) {
        Map<String, JavaPropertyDescriptor> properties = new LinkedHashMap<>();
        if (javaType.isRecord()) {
            for (RecordComponent recordComponent : javaType.getRecordComponents()) {
                addProperty(properties, recordComponent.getName(), "record-accessor", recordComponent.getAccessor());
            }
        }
        for (Method method : sortedPublicMethods(javaType)) {
            String propertyName = propertyName(method);
            if (propertyName != null) {
                addProperty(properties, propertyName, "bean-getter", method);
            }
        }
        return properties;
    }

    private static void addProperty(
            Map<String, JavaPropertyDescriptor> properties,
            String propertyName,
            String kind,
            Method method) {
        JavaPropertyDescriptor descriptor = createPropertyDescriptor(propertyName, kind, method);
        JavaPropertyDescriptor previous = properties.putIfAbsent(descriptor.name(), descriptor);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate Java property name: " + descriptor.name());
        }
    }

    private static JavaPropertyDescriptor createPropertyDescriptor(String propertyName, String kind, Method method) {
        ExpressionType type = JavaMemberTypes.expressionType(method.getGenericReturnType(), method.getReturnType(), true);
        try {
            MethodHandle methodHandle = adaptSequenceReturn(LOOKUP.unreflect(method), type);
            MethodHandle handle = FunctionHandleAdapters.adaptInstance(
                    methodHandle,
                    method.getDeclaringClass(),
                    List.of(),
                    type);
            return new JavaPropertyDescriptor(
                    propertyName,
                    type,
                    handle,
                    JavaMemberImplementationMetadata.forMethod(kind, method));
        } catch (IllegalAccessException exception) {
            throw new IllegalArgumentException("Java property accessor is not accessible: " + method, exception);
        }
    }

    private static JavaMethodDescriptor createMethodDescriptor(Class<?> javaType, Method method, boolean allowVarargs) {
        List<ExpressionType> parameterTypes = parameterTypes(method, allowVarargs);
        ExpressionType returnType = JavaMemberTypes.expressionType(method.getGenericReturnType(), method.getReturnType(), true);
        try {
            MethodHandle methodHandle = LOOKUP.unreflect(method);
            methodHandle = adaptSequenceArguments(methodHandle, method, parameterTypes);
            methodHandle = adaptSequenceReturn(methodHandle, returnType);
            MethodHandle handle = FunctionHandleAdapters.adaptInstance(
                    methodHandle,
                    javaType,
                    parameterTypes,
                    returnType);
            return new JavaMethodDescriptor(
                    method.getName(),
                    parameterTypes,
                    returnType,
                    handle,
                    JavaMemberImplementationMetadata.forMethod("method", method));
        } catch (IllegalAccessException exception) {
            throw new IllegalArgumentException("Java member method is not accessible: " + method, exception);
        }
    }

    private static MethodHandle adaptSequenceArguments(
            MethodHandle methodHandle,
            Method method,
            List<ExpressionType> parameterTypes) {
        MethodHandle adapted = methodHandle;
        Class<?>[] javaParameterTypes = method.getParameterTypes();
        for (int index = 0; index < parameterTypes.size(); index++) {
            if (parameterTypes.get(index) instanceof CollectionType) {
                Class<?> javaType = javaParameterTypes[index];
                Class<?> elementType = sequenceElementType(method.getGenericParameterTypes()[index], javaType);
                MethodHandle adapter = MethodHandles.insertArguments(SEQUENCE_ARGUMENT, 1, javaType, elementType)
                        .asType(MethodType.methodType(javaType, List.class));
                adapted = MethodHandles.filterArguments(adapted, index + 1, adapter);
            }
        }
        return adapted;
    }

    private static MethodHandle adaptSequenceReturn(MethodHandle methodHandle, ExpressionType returnType) {
        if (!(returnType instanceof CollectionType collectionType)) {
            return methodHandle;
        }
        MethodHandle adapter = MethodHandles.insertArguments(SEQUENCE_RESULT, 1, collectionType.elementType())
                .asType(MethodType.methodType(
                List.class,
                methodHandle.type().returnType()));
        return MethodHandles.filterReturnValue(methodHandle, adapter);
    }

    private static Object sequenceArgument(List<?> values, Class<?> javaType, Class<?> elementType) {
        if (javaType.isArray()) {
            return collectionToArray(values, javaType);
        }
        List<?> converted = values.stream()
                .map(value -> value instanceof java.math.BigDecimal number
                        ? FunctionHandleAdapters.adaptNumericValue(number, elementType)
                        : value)
                .toList();
        if (javaType.isAssignableFrom(List.class)) {
            return converted;
        }
        return BoundaryCoercion.standard().prepareJavaConversion(List.class, javaType).convert(converted);
    }

    private static Class<?> sequenceElementType(java.lang.reflect.Type genericType, Class<?> javaType) {
        if (javaType.isArray()) {
            return javaType.getComponentType();
        }
        if (genericType instanceof java.lang.reflect.ParameterizedType parameterizedType
                && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> elementType) {
            return elementType;
        }
        throw new IllegalArgumentException("Java collection member must declare a concrete element type");
    }

    private static List<?> sequenceResult(Object value, ExpressionType elementType) {
        Objects.requireNonNull(value, "Java member results must not be null");
        ArrayList<Object> snapshot = new ArrayList<>();
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                snapshot.add(BoundaryCoercion.standard()
                        .convertFunctionBindingFallback(Array.get(value, index), elementType));
            }
        } else if (value instanceof Iterable<?> iterable) {
            iterable.forEach(item -> snapshot.add(BoundaryCoercion.standard()
                    .convertFunctionBindingFallback(item, elementType)));
        } else {
            throw new IllegalArgumentException("Java collection member result must be an array or Iterable");
        }
        return List.copyOf(snapshot);
    }

    private static Object collectionToArray(List<?> values, Class<?> arrayType) {
        Objects.requireNonNull(values, "values");
        Object array = Array.newInstance(arrayType.getComponentType(), values.size());
        for (int index = 0; index < values.size(); index++) {
            Array.set(array, index, adaptArrayElement(values.get(index), arrayType.getComponentType()));
        }
        return array;
    }

    private static Object adaptArrayElement(Object value, Class<?> componentType) {
        return value instanceof java.math.BigDecimal number
                ? FunctionHandleAdapters.adaptNumericValue(number, componentType)
                : value;
    }

    private static MethodHandle findCollectionToArray() {
        try {
            return LOOKUP.findStatic(
                    JavaTypeCatalog.class,
                    "collectionToArray",
                    MethodType.methodType(Object.class, List.class, Class.class));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("failed to resolve varargs adapter", exception);
        }
    }

    private static MethodHandle findSequenceArgument() {
        try {
            return LOOKUP.findStatic(
                    JavaTypeCatalog.class,
                    "sequenceArgument",
                    MethodType.methodType(Object.class, List.class, Class.class, Class.class));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("failed to resolve sequence argument adapter", exception);
        }
    }

    private static MethodHandle findSequenceResult() {
        try {
            return LOOKUP.findStatic(
                    JavaTypeCatalog.class,
                    "sequenceResult",
                    MethodType.methodType(List.class, Object.class, ExpressionType.class));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("failed to resolve sequence result adapter", exception);
        }
    }

    private static List<ExpressionType> parameterTypes(Method method, boolean allowVarargs) {
        List<ExpressionType> parameterTypes = new ArrayList<>(method.getParameterCount());
        Class<?>[] rawParameterTypes = method.getParameterTypes();
        java.lang.reflect.Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (int index = 0; index < rawParameterTypes.length; index++) {
            parameterTypes.add(JavaMemberTypes.expressionType(
                    genericParameterTypes[index],
                    rawParameterTypes[index],
                    true));
        }
        return List.copyOf(parameterTypes);
    }

    private static boolean isPublicMethodCandidate(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType() != void.class
                && !method.isBridge()
                && !method.isSynthetic()
                && !method.isVarArgs()
                && !isObjectMethod(method);
    }

    private static boolean isSupportedPublicMethod(Method method) {
        if (JavaMemberTypes.tryExpressionType(method.getGenericReturnType(), method.getReturnType(), true).isEmpty()) {
            return false;
        }
        Class<?>[] rawParameterTypes = method.getParameterTypes();
        java.lang.reflect.Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (int index = 0; index < rawParameterTypes.length; index++) {
            if (JavaMemberTypes.tryExpressionType(genericParameterTypes[index], rawParameterTypes[index], true).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static List<Method> sortedPublicMethods(Class<?> javaType) {
        List<Method> methods = new ArrayList<>(List.of(javaType.getMethods()));
        methods.sort(JavaTypeCatalog::compareMethods);
        return methods;
    }

    private static int compareMethods(Method first, Method second) {
        int nameComparison = first.getName().compareTo(second.getName());
        if (nameComparison != 0) {
            return nameComparison;
        }
        int arityComparison = Integer.compare(first.getParameterCount(), second.getParameterCount());
        if (arityComparison != 0) {
            return arityComparison;
        }
        int descriptorComparison = first.toGenericString().compareTo(second.toGenericString());
        if (descriptorComparison != 0) {
            return descriptorComparison;
        }
        return first.toString().compareTo(second.toString());
    }

    private static String propertyName(Method method) {
        if (!isPropertyAccessorCandidate(method)) {
            return null;
        }
        String methodName = method.getName();
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return decapitalize(methodName.substring(2));
        }
        return null;
    }

    private static boolean isPropertyAccessorCandidate(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getParameterCount() == 0
                && method.getReturnType() != void.class
                && !method.isBridge()
                && !method.isSynthetic()
                && !isObjectMethod(method);
    }

    private static boolean isObjectMethod(Method method) {
        if (method.getDeclaringClass() == Object.class) {
            return true;
        }
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        return (methodName.equals("toString") && parameterTypes.length == 0)
                || (methodName.equals("hashCode") && parameterTypes.length == 0)
                || (methodName.equals("equals") && parameterTypes.length == 1 && parameterTypes[0] == Object.class);
    }

    private static String decapitalize(String value) {
        if (value.length() > 1 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1))) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private record MethodKey(String name, List<Class<?>> parameterTypes) {

        private MethodKey(String name, Class<?>... parameterTypes) {
            this(FunctionSignature.validateLanguageName(name), copyParameterTypes(parameterTypes));
        }

        private Method resolve(Class<?> javaType) {
            try {
                return javaType.getMethod(name, parameterTypes.toArray(Class<?>[]::new));
            } catch (NoSuchMethodException exception) {
                throw new IllegalArgumentException("selected Java method has no target: " + name + parameterTypes, exception);
            }
        }

        private static List<Class<?>> copyParameterTypes(Class<?>[] parameterTypes) {
            Objects.requireNonNull(parameterTypes, "parameterTypes");
            ArrayList<Class<?>> copy = new ArrayList<>(parameterTypes.length);
            for (Class<?> parameterType : parameterTypes) {
                copy.add(Objects.requireNonNull(parameterType, "parameterType"));
            }
            return List.copyOf(copy);
        }
    }
}
