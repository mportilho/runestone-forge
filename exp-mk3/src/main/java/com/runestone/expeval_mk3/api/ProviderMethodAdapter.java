package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.stream.BaseStream;

final class ProviderMethodAdapter {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle CONVERT = findConvert();
    private static final String NON_NULL_MESSAGE = "function arguments and results must not be null";

    private ProviderMethodAdapter() {
    }

    static PreparedMethod prepare(
            Method method,
            JavaTypeCatalog javaTypes,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(javaTypes, "javaTypes");
        Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        if (method.isVarArgs()) {
            throw new IllegalArgumentException("varargs provider methods are not supported: " + method);
        }
        if (method.getTypeParameters().length > 0) {
            throw new IllegalArgumentException("generic provider methods are not supported: " + method);
        }

        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Class<?>[] parameterTypes = method.getParameterTypes();
        ArrayList<PreparedValue> parameters = new ArrayList<>(parameterTypes.length);
        for (int index = 0; index < parameterTypes.length; index++) {
            parameters.add(prepareValue(
                    genericParameterTypes[index],
                    parameterTypes[index],
                    Direction.ARGUMENT,
                    javaTypes,
                    boundaryCoercion,
                    maxMaterializedSize));
        }
        PreparedValue result = prepareValue(
                method.getGenericReturnType(),
                method.getReturnType(),
                Direction.RESULT,
                javaTypes,
                boundaryCoercion,
                maxMaterializedSize);
        return new PreparedMethod(List.copyOf(parameters), result);
    }

    private static PreparedValue prepareValue(
            Type genericType,
            Class<?> rawType,
            Direction direction,
            JavaTypeCatalog javaTypes,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        rejectUnresolved(genericType);
        if (rawType == void.class || rawType == Void.class) {
            throw new IllegalArgumentException("void provider method returns are not supported");
        }
        if (rawType.isArray()) {
            return prepareArray(genericType, rawType, direction, javaTypes, boundaryCoercion, maxMaterializedSize);
        }
        if (isCanonicalScalar(rawType)) {
            return scalar(rawType);
        }
        if (isNumeric(rawType)) {
            return numeric(rawType, direction, boundaryCoercion);
        }
        if (Optional.class.isAssignableFrom(rawType)
                || rawType == OptionalInt.class
                || rawType == OptionalLong.class
                || rawType == OptionalDouble.class
                || BaseStream.class.isAssignableFrom(rawType)
                || Future.class.isAssignableFrom(rawType)
                || CompletionStage.class.isAssignableFrom(rawType)
                || Flow.Publisher.class.isAssignableFrom(rawType)) {
            throw new IllegalArgumentException("optional, stream, and asynchronous provider method types are not supported: "
                    + rawType.getName());
        }

        JavaTypeDescriptor registeredType = javaTypes.find(rawType).orElse(null);
        if (registeredType != null) {
            if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
                throw new IllegalArgumentException("registered nominal provider method types must be concrete: "
                        + rawType.getName());
            }
            return nominal(rawType, registeredType.objectType());
        }

        ContainerKind containerKind = ContainerKind.of(rawType);
        if (containerKind != null) {
            return prepareContainer(
                    genericType,
                    rawType,
                    containerKind,
                    direction,
                    javaTypes,
                    boundaryCoercion,
                    maxMaterializedSize);
        }
        if (rawType == Object.class) {
            throw new IllegalArgumentException("Object provider method types are not supported");
        }
        throw new IllegalArgumentException(
                "unsupported provider method type; not a registered Java type: " + rawType.getName());
    }

    private static PreparedValue prepareArray(
            Type genericType,
            Class<?> rawType,
            Direction direction,
            JavaTypeCatalog javaTypes,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        Class<?> componentType = rawType.getComponentType();
        if (componentType.isArray()) {
            throw new IllegalArgumentException("multidimensional array provider method types are not supported");
        }
        Type genericComponentType = genericType instanceof GenericArrayType genericArrayType
                ? genericArrayType.getGenericComponentType()
                : componentType;
        PreparedValue element = prepareValue(
                genericComponentType,
                rawClass(genericComponentType),
                direction,
                javaTypes,
                boundaryCoercion,
                maxMaterializedSize);
        ValueConversion conversion = direction == Direction.ARGUMENT
                ? value -> toArray(value, componentType, element)
                : value -> arrayResult(value, element, maxMaterializedSize);
        return new PreparedValue(new CollectionType(element.expressionType()), conversion);
    }

    private static PreparedValue prepareContainer(
            Type genericType,
            Class<?> rawType,
            ContainerKind kind,
            Direction direction,
            JavaTypeCatalog javaTypes,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException("raw " + rawType.getName() + " provider method types are not supported");
        }
        Type[] arguments = parameterizedType.getActualTypeArguments();
        if (kind == ContainerKind.MAP) {
            if (arguments.length != 2 || arguments[0] != String.class) {
                throw new IllegalArgumentException("provider map types require declared String keys");
            }
            Type valueType = arguments[1];
            PreparedValue mapValue = prepareValue(
                    valueType,
                    rawClass(valueType),
                    direction,
                    javaTypes,
                    boundaryCoercion,
                    maxMaterializedSize);
            ValueConversion conversion = direction == Direction.ARGUMENT
                    ? mapArgument(rawType, mapValue, boundaryCoercion)
                    : value -> mapResult(value, mapValue, maxMaterializedSize);
            return new PreparedValue(new MapType(mapValue.expressionType()), conversion);
        }
        if (arguments.length != 1) {
            throw new IllegalArgumentException("container provider method type must declare one element type");
        }
        Type elementType = arguments[0];
        PreparedValue element = prepareValue(
                elementType,
                rawClass(elementType),
                direction,
                javaTypes,
                boundaryCoercion,
                maxMaterializedSize);
        ExpressionType expressionType = new CollectionType(element.expressionType());
        ValueConversion conversion = direction == Direction.ARGUMENT
                ? sequenceArgument(rawType, element, boundaryCoercion)
                : value -> sequenceResult(value, element, maxMaterializedSize);
        return new PreparedValue(expressionType, conversion);
    }

    private static PreparedValue scalar(Class<?> rawType) {
        ExpressionType expressionType;
        if (rawType == boolean.class || rawType == Boolean.class) {
            expressionType = ScalarType.BOOLEAN;
        } else if (rawType == String.class) {
            expressionType = ScalarType.STRING;
        } else if (rawType == LocalDate.class) {
            expressionType = ScalarType.DATE;
        } else if (rawType == LocalTime.class) {
            expressionType = ScalarType.TIME;
        } else {
            expressionType = ScalarType.DATETIME;
        }
        Class<?> boxedType = ExpressionJavaTypes.boxed(rawType);
        return new PreparedValue(expressionType, value -> requireInstance(value, boxedType));
    }

    private static PreparedValue numeric(
            Class<?> rawType,
            Direction direction,
            BoundaryCoercion boundaryCoercion) {
        BoundaryCoercion.PreparedJavaConversion conversion = direction == Direction.ARGUMENT
                ? boundaryCoercion.prepareJavaConversion(BigDecimal.class, rawType)
                : boundaryCoercion.prepareJavaConversion(rawType, BigDecimal.class);
        return new PreparedValue(ScalarType.NUMBER, conversion::convert);
    }

    private static PreparedValue nominal(Class<?> rawType, ObjectType objectType) {
        return new PreparedValue(objectType, value -> {
            requireNonNull(value);
            if (value.getClass() != rawType) {
                throw new IllegalArgumentException("provider nominal value must have exact runtime class "
                        + rawType.getName());
            }
            return value;
        });
    }

    private static ValueConversion sequenceArgument(
            Class<?> rawType,
            PreparedValue element,
            BoundaryCoercion boundaryCoercion) {
        OuterConversion outer = prepareSequenceOuterConversion(rawType, boundaryCoercion);
        return value -> {
            List<Object> converted = convertCanonicalSequence(value, element);
            return outer.convert(Collections.unmodifiableList(converted));
        };
    }

    private static ValueConversion mapArgument(
            Class<?> rawType,
            PreparedValue mapValue,
            BoundaryCoercion boundaryCoercion) {
        OuterConversion outer = prepareMapOuterConversion(rawType, boundaryCoercion);
        return value -> {
            Map<String, Object> converted = convertCanonicalMap(value, mapValue);
            return outer.convert(Collections.unmodifiableMap(converted));
        };
    }

    private static OuterConversion prepareSequenceOuterConversion(
            Class<?> rawType,
            BoundaryCoercion boundaryCoercion) {
        if (rawType.isAssignableFrom(List.class)) {
            return value -> value;
        }
        return boundaryCoercion.prepareJavaConversion(List.class, rawType)::convert;
    }

    private static OuterConversion prepareMapOuterConversion(
            Class<?> rawType,
            BoundaryCoercion boundaryCoercion) {
        if (rawType.isAssignableFrom(Map.class)) {
            return value -> value;
        }
        return boundaryCoercion.prepareJavaConversion(Map.class, rawType)::convert;
    }

    private static List<Object> convertCanonicalSequence(Object value, PreparedValue element) throws Throwable {
        requireNonNull(value);
        if (!(value instanceof List<?> values)) {
            throw new IllegalArgumentException("canonical collection values must be lists");
        }
        ArrayList<Object> converted = new ArrayList<>(values.size());
        for (Object item : values) {
            converted.add(element.convert(item));
        }
        return converted;
    }

    private static Map<String, Object> convertCanonicalMap(Object value, PreparedValue mapValue) throws Throwable {
        requireNonNull(value);
        if (!(value instanceof Map<?, ?> values)) {
            throw new IllegalArgumentException("canonical map values must be maps");
        }
        LinkedHashMap<String, Object> converted = new LinkedHashMap<>(values.size());
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("provider maps must have non-null String keys");
            }
            converted.put(key, mapValue.convert(entry.getValue()));
        }
        return converted;
    }

    private static Object toArray(Object value, Class<?> componentType, PreparedValue element) throws Throwable {
        requireNonNull(value);
        if (!(value instanceof List<?> values)) {
            throw new IllegalArgumentException("canonical collection values must be lists");
        }
        Object array = Array.newInstance(componentType, values.size());
        for (int index = 0; index < values.size(); index++) {
            Array.set(array, index, element.convert(values.get(index)));
        }
        return array;
    }

    private static Object arrayResult(Object value, PreparedValue element, int maxMaterializedSize) throws Throwable {
        requireNonNull(value);
        int length = Array.getLength(value);
        requireWithinLimit(length, maxMaterializedSize);
        ArrayList<Object> converted = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            converted.add(element.convert(Array.get(value, index)));
        }
        return Collections.unmodifiableList(converted);
    }

    private static Object sequenceResult(Object value, PreparedValue element, int maxMaterializedSize) throws Throwable {
        requireNonNull(value);
        if (!(value instanceof Iterable<?> iterable)) {
            throw new IllegalArgumentException("provider collection result must be iterable");
        }
        if (value instanceof Collection<?> collection) {
            requireWithinLimit(collection.size(), maxMaterializedSize);
        }
        ArrayList<Object> converted = new ArrayList<>();
        Iterator<?> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (converted.size() == maxMaterializedSize) {
                throw materializationLimitExceeded(maxMaterializedSize);
            }
            converted.add(element.convert(next));
        }
        return Collections.unmodifiableList(converted);
    }

    private static Object mapResult(Object value, PreparedValue mapValue, int maxMaterializedSize) throws Throwable {
        requireNonNull(value);
        if (!(value instanceof Map<?, ?> values)) {
            throw new IllegalArgumentException("provider map result must be a map");
        }
        requireWithinLimit(values.size(), maxMaterializedSize);
        LinkedHashMap<String, Object> converted = new LinkedHashMap<>(values.size());
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("provider maps must have non-null String keys");
            }
            converted.put(key, mapValue.convert(entry.getValue()));
        }
        return Collections.unmodifiableMap(converted);
    }

    private static void requireWithinLimit(int size, int maxMaterializedSize) {
        if (size > maxMaterializedSize) {
            throw materializationLimitExceeded(maxMaterializedSize);
        }
    }

    private static IllegalArgumentException materializationLimitExceeded(int maxMaterializedSize) {
        return new IllegalArgumentException("provider result exceeds maxMaterializedSize " + maxMaterializedSize);
    }

    private static Object requireInstance(Object value, Class<?> expectedType) {
        requireNonNull(value);
        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException("provider boundary value must be an instance of " + expectedType.getName());
        }
        return value;
    }

    private static void requireNonNull(Object value) {
        Objects.requireNonNull(value, NON_NULL_MESSAGE);
    }

    private static void rejectUnresolved(Type type) {
        if (type instanceof WildcardType) {
            throw new IllegalArgumentException("wildcard provider method types are not supported");
        }
        if (type instanceof TypeVariable<?>) {
            throw new IllegalArgumentException("unresolved provider method type variables are not supported");
        }
    }

    private static Class<?> rawClass(Type type) {
        rejectUnresolved(type);
        if (type instanceof Class<?> rawClass) {
            return rawClass;
        }
        if (type instanceof ParameterizedType parameterizedType
                && parameterizedType.getRawType() instanceof Class<?> rawClass) {
            return rawClass;
        }
        if (type instanceof GenericArrayType genericArrayType) {
            Class<?> componentType = rawClass(genericArrayType.getGenericComponentType());
            return Array.newInstance(componentType, 0).getClass();
        }
        throw new IllegalArgumentException("unresolvable provider method type is not supported: " + type.getTypeName());
    }

    private static boolean isCanonicalScalar(Class<?> rawType) {
        return rawType == boolean.class
                || rawType == Boolean.class
                || rawType == String.class
                || rawType == LocalDate.class
                || rawType == LocalTime.class
                || rawType == LocalDateTime.class;
    }

    private static boolean isNumeric(Class<?> rawType) {
        return Number.class.isAssignableFrom(ExpressionJavaTypes.boxed(rawType));
    }

    private static MethodHandle findConvert() {
        try {
            return LOOKUP.findVirtual(
                    PreparedValue.class,
                    "convert",
                    MethodType.methodType(Object.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    record PreparedMethod(List<PreparedValue> parameters, PreparedValue result) {

        PreparedMethod {
            parameters = List.copyOf(parameters);
            Objects.requireNonNull(result, "result");
        }

        List<ExpressionType> parameterTypes() {
            return parameters.stream().map(PreparedValue::expressionType).toList();
        }

        ExpressionType returnType() {
            return result.expressionType();
        }

        MethodHandle adapt(MethodHandle implementation) {
            MethodHandle adapted = implementation;
            for (int index = 0; index < parameters.size(); index++) {
                PreparedValue parameter = parameters.get(index);
                MethodHandle filter = CONVERT.bindTo(parameter).asType(MethodType.methodType(
                        implementation.type().parameterType(index),
                        ExpressionJavaTypes.valueType(parameter.expressionType())));
                adapted = MethodHandles.filterArguments(adapted, index, filter);
            }
            MethodHandle resultFilter = CONVERT.bindTo(result).asType(MethodType.methodType(
                    ExpressionJavaTypes.valueType(result.expressionType()),
                    implementation.type().returnType()));
            return MethodHandles.filterReturnValue(adapted, resultFilter);
        }
    }

    record PreparedValue(ExpressionType expressionType, ValueConversion conversion) {

        PreparedValue {
            Objects.requireNonNull(expressionType, "expressionType");
            Objects.requireNonNull(conversion, "conversion");
        }

        Object convert(Object value) throws Throwable {
            return conversion.convert(value);
        }
    }

    @FunctionalInterface
    private interface ValueConversion {
        Object convert(Object value) throws Throwable;
    }

    @FunctionalInterface
    private interface OuterConversion {
        Object convert(Object value) throws Throwable;
    }

    private enum Direction {
        ARGUMENT,
        RESULT
    }

    private enum ContainerKind {
        COLLECTION,
        MAP;

        private static ContainerKind of(Class<?> rawType) {
            if (Map.class.isAssignableFrom(rawType)) {
                return MAP;
            }
            if (Collection.class.isAssignableFrom(rawType) || Iterable.class.isAssignableFrom(rawType)) {
                return COLLECTION;
            }
            return null;
        }
    }
}
