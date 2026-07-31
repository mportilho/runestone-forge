package com.runestone.expeval_mk3.api;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.PreparedDataConversion;
import com.runestone.converters.impl.stable.DefaultDataConversionService;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.ProviderReturnViolation;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Coercao de Borda profile used only at explicit expression API boundaries.
 */
public final class BoundaryCoercion {

    static final int DEFAULT_MAX_MATERIALIZED_SIZE = 10_000;

    private static final BoundaryCoercion STANDARD = new BoundaryCoercion(
            DefaultDataConversionService.standard());

    private final String profileIdentity;
    private final String profileHash;
    private final DataConversionService dataConversionService;

    private BoundaryCoercion(DataConversionService dataConversionService) {
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService");
        profileIdentity = validateProfileIdentity(dataConversionService.conversionProfileIdentity());
        profileHash = validateProfileHash(dataConversionService.conversionProfileHash());
    }

    public static BoundaryCoercion standard() {
        return STANDARD;
    }

    public static BoundaryCoercion of(DataConversionService dataConversionService) {
        return new BoundaryCoercion(dataConversionService);
    }

    public String profileIdentity() {
        return profileIdentity;
    }

    public String profileHash() {
        return profileHash;
    }

    boolean deterministicForConstants() {
        return true;
    }

    public boolean canConvert(Class<?> sourceType, ExpressionType targetType) {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(targetType, "targetType");
        return switch (targetType) {
            case ScalarType scalarType -> canConvertScalarType(sourceType, scalarType);
            case CollectionType collectionType -> canConvertCollectionType(sourceType, collectionType.elementType());
            case MapType mapType -> canConvertMapType(sourceType, mapType.valueType());
            case ObjectType objectType -> sourceType.getName().equals(objectType.name());
        };
    }

    public boolean canConvert(Object sourceValue, ExpressionType targetType) {
        Objects.requireNonNull(targetType, "targetType");
        try {
            convertBoundaryValue("boundary value", sourceValue, targetType, DEFAULT_MAX_MATERIALIZED_SIZE, true);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    PreparedJavaConversion prepareJavaConversion(Class<?> sourceType, Class<?> targetType) {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(targetType, "targetType");
        Class<?> boxedSourceType = ExpressionJavaTypes.boxed(sourceType);
        Class<?> boxedTargetType = ExpressionJavaTypes.boxed(targetType);
        if (boxedTargetType.isAssignableFrom(boxedSourceType)) {
            return value -> requireConversionValue(value, boxedSourceType, boxedTargetType);
        }
        PreparedJavaConversion containerConversion = prepareContainerConversion(
                boxedSourceType, boxedTargetType);
        if (containerConversion != null) {
            return containerConversion;
        }
        if (!dataConversionService.canPrepareConversion(boxedSourceType, boxedTargetType)) {
            throw new IllegalArgumentException("configured boundary coercion does not support "
                    + boxedSourceType.getName() + " -> " + boxedTargetType.getName());
        }
        PreparedDataConversion conversion = dataConversionService.prepareConversion(
                boxedSourceType, boxedTargetType);
        return value -> {
            requireConversionValue(value, boxedSourceType, boxedSourceType);
            Object converted = conversion.convert(value);
            return requireConversionValue(converted, boxedTargetType, boxedTargetType);
        };
    }

    public Object convertFunctionBindingFallback(Object sourceValue, ExpressionType targetType) {
        Objects.requireNonNull(targetType, "targetType");
        return convertBoundaryValue(
                "function-binding fallback value",
                sourceValue,
                targetType,
                Integer.MAX_VALUE,
                false);
    }

    Object convertDefault(String symbolName, Object sourceValue, ExpressionType targetType, int maxMaterializedSize) {
        return convertBoundaryValue(
                "external symbol '" + symbolName + "' default",
                sourceValue,
                targetType,
                maxMaterializedSize,
                true);
    }

    Object convertOverride(String symbolName, Object sourceValue, ExpressionType targetType, int maxMaterializedSize) {
        return convertBoundaryValue(
                "external symbol '" + symbolName + "' override",
                sourceValue,
                targetType,
                maxMaterializedSize,
                true);
    }

    private Object convertBoundaryValue(
            String boundaryName,
            Object sourceValue,
            ExpressionType targetType,
            int maxMaterializedSize,
            boolean iterableAllowed) {
        try {
            return convertValue(boundaryName, sourceValue, targetType, maxMaterializedSize, iterableAllowed);
        } catch (RuntimeException exception) {
            throw new BoundaryCoercionFailure(
                    boundaryName + " cannot be converted to " + targetType + ": " + exception.getMessage(),
                    exception);
        }
    }

    private Object convertValue(
            String valueName,
            Object sourceValue,
            ExpressionType targetType,
            int maxMaterializedSize,
            boolean iterableAllowed) {
        Objects.requireNonNull(targetType, "targetType");
        return switch (targetType) {
            case ScalarType scalarType -> convertScalar(valueName, sourceValue, scalarType);
            case CollectionType collectionType -> convertCollection(
                    valueName,
                    sourceValue,
                    collectionType.elementType(),
                    maxMaterializedSize,
                    iterableAllowed);
            case MapType mapType -> convertMap(
                    valueName, sourceValue, mapType.valueType(), maxMaterializedSize, iterableAllowed);
            case ObjectType objectType -> convertObject(valueName, sourceValue, objectType);
        };
    }

    private Object convertScalar(String valueName, Object sourceValue, ScalarType scalarType) {
        if (sourceValue == null) {
            throw new IllegalArgumentException("scalar target requires a non-null value");
        }
        Class<?> targetClass = ExpressionJavaTypes.scalarValueType(scalarType);
        if (targetClass.isInstance(sourceValue)) {
            return sourceValue;
        }
        Object converted = dataConversionService.convert(sourceValue, targetClass);
        if (converted == null || !targetClass.isInstance(converted)) {
            throw new IllegalArgumentException("conversion to "
                    + scalarType
                    + " returned "
                    + (converted == null ? "null" : converted.getClass().getName()));
        }
        if (scalarType == ScalarType.NUMBER && !(converted instanceof BigDecimal)) {
            throw new IllegalArgumentException("numeric boundary coercion did not produce BigDecimal for " + valueName);
        }
        return converted;
    }

    private boolean canConvertScalarType(Class<?> sourceType, ScalarType scalarType) {
        Class<?> targetClass = ExpressionJavaTypes.scalarValueType(scalarType);
        Class<?> effectiveSourceType = ExpressionJavaTypes.boxed(sourceType);
        return targetClass.isAssignableFrom(effectiveSourceType)
                || dataConversionService.canConvert(effectiveSourceType, targetClass);
    }

    private boolean canConvertCollectionType(Class<?> sourceType, ExpressionType elementType) {
        if (Collection.class.isAssignableFrom(sourceType)) {
            return false;
        }
        if (!sourceType.isArray()) {
            return false;
        }
        return canConvert(sourceType.getComponentType(), elementType);
    }

    private static boolean canConvertMapType(Class<?> sourceType, ExpressionType valueType) {
        Objects.requireNonNull(valueType, "valueType");
        return false;
    }

    private static Object requireConversionValue(Object value, Class<?> sourceType, Class<?> targetType) {
        if (value == null) {
            throw new ProviderReturnViolation(
                    DiagnosticCode.RUNTIME_RETURN_NULL, "function arguments and results must not be null");
        }
        if (!sourceType.isInstance(value)) {
            throw new ProviderReturnViolation(
                    DiagnosticCode.RUNTIME_RETURN_TYPE_MISMATCH,
                    "boundary value must be an instance of " + sourceType.getName());
        }
        if (!targetType.isInstance(value)) {
            throw new ProviderReturnViolation(
                    DiagnosticCode.RUNTIME_RETURN_TYPE_MISMATCH,
                    "boundary conversion did not produce " + targetType.getName());
        }
        return value;
    }

    private static PreparedJavaConversion prepareContainerConversion(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == List.class && targetType == ArrayList.class) {
            return value -> new ArrayList<>((List<?>) requireConversionValue(value, List.class, List.class));
        }
        if (sourceType == List.class && targetType == Set.class) {
            return value -> Collections.unmodifiableSet(
                    new LinkedHashSet<>((List<?>) requireConversionValue(value, List.class, List.class)));
        }
        if (sourceType == List.class && targetType == LinkedHashSet.class) {
            return value -> new LinkedHashSet<>((List<?>) requireConversionValue(value, List.class, List.class));
        }
        if (sourceType == Map.class && targetType == LinkedHashMap.class) {
            return value -> new LinkedHashMap<>((Map<?, ?>) requireConversionValue(value, Map.class, Map.class));
        }
        return null;
    }

    private Object convertCollection(
            String valueName,
            Object sourceValue,
            ExpressionType elementType,
            int maxMaterializedSize,
            boolean iterableAllowed) {
        if (sourceValue instanceof Collection<?> values) {
            requireWithinLimit(valueName, values.size(), maxMaterializedSize);
            ArrayList<Object> convertedValues = new ArrayList<>(values.size());
            for (Object element : values) {
                if (convertedValues.size() == maxMaterializedSize) {
                    throw materializationLimitExceeded(valueName, maxMaterializedSize);
                }
                convertedValues.add(convertElement(
                        valueName, elementType, element, maxMaterializedSize, iterableAllowed));
            }
            return Collections.unmodifiableList(convertedValues);
        }
        if (sourceValue != null && sourceValue.getClass().isArray()) {
            int length = Array.getLength(sourceValue);
            requireWithinLimit(valueName, length, maxMaterializedSize);
            ArrayList<Object> convertedValues = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                convertedValues.add(convertElement(
                        valueName,
                        elementType,
                        Array.get(sourceValue, index),
                        maxMaterializedSize,
                        iterableAllowed));
            }
            return Collections.unmodifiableList(convertedValues);
        }
        if (iterableAllowed && sourceValue instanceof Iterable<?> values) {
            ArrayList<Object> convertedValues = new ArrayList<>(Math.min(maxMaterializedSize, 10));
            for (Object element : values) {
                if (convertedValues.size() == maxMaterializedSize) {
                    throw materializationLimitExceeded(valueName, maxMaterializedSize);
                }
                convertedValues.add(convertElement(
                        valueName, elementType, element, maxMaterializedSize, iterableAllowed));
            }
            return Collections.unmodifiableList(convertedValues);
        }
        throw new IllegalArgumentException("value must be a collection for CollectionType");
    }

    private Object convertElement(
            String valueName,
            ExpressionType elementType,
            Object element,
            int maxMaterializedSize,
            boolean iterableAllowed) {
        return convertValue(valueName, element, elementType, maxMaterializedSize, iterableAllowed);
    }

    private Map<String, Object> convertMap(
            String valueName,
            Object sourceValue,
            ExpressionType valueType,
            int maxMaterializedSize,
            boolean iterableAllowed) {
        if (!(sourceValue instanceof Map<?, ?> values)) {
            throw new IllegalArgumentException("value must be a map for MapType");
        }
        requireWithinLimit(valueName, values.size(), maxMaterializedSize);
        TreeMap<String, Object> sortedValues = new TreeMap<>();
        int entryCount = 0;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (entryCount == maxMaterializedSize) {
                throw materializationLimitExceeded(valueName, maxMaterializedSize);
            }
            entryCount++;
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("MapType values must be text-keyed");
            }
            sortedValues.put(key, convertElement(
                    valueName, valueType, entry.getValue(), maxMaterializedSize, iterableAllowed));
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(sortedValues));
    }

    private static void requireWithinLimit(String valueName, int size, int maxMaterializedSize) {
        if (size > maxMaterializedSize) {
            throw materializationLimitExceeded(valueName, maxMaterializedSize);
        }
    }

    private static IllegalArgumentException materializationLimitExceeded(
            String valueName,
            int maxMaterializedSize) {
        return new IllegalArgumentException(
                valueName + " exceeds maxMaterializedSize " + maxMaterializedSize);
    }

    private Object convertObject(String valueName, Object sourceValue, ObjectType objectType) {
        if (sourceValue == null) {
            throw new IllegalArgumentException("object target requires a non-null value");
        }
        if (!sourceValue.getClass().getName().equals(objectType.name())) {
            throw new IllegalArgumentException(valueName + " must be an instance of " + objectType.name());
        }
        return sourceValue;
    }

    static String validateProfileIdentity(String profileIdentity) {
        Objects.requireNonNull(profileIdentity, "profileIdentity");
        if (profileIdentity.isBlank()) {
            throw new IllegalArgumentException("conversion profile identity must not be blank");
        }
        return profileIdentity;
    }

    static String validateProfileHash(String profileHash) {
        Objects.requireNonNull(profileHash, "profileHash");
        if (profileHash.isBlank()) {
            throw new IllegalArgumentException("conversion profile hash must not be blank");
        }
        return profileHash;
    }

    private static final class BoundaryCoercionFailure extends IllegalArgumentException {

        private BoundaryCoercionFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @FunctionalInterface
    interface PreparedJavaConversion {
        Object convert(Object value);
    }
}
