package com.runestone.expeval_mk3.api;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Coercao de Borda profile used only at explicit expression API boundaries.
 */
public final class BoundaryCoercion {

    private static final BoundaryCoercion STANDARD = new BoundaryCoercion(
            ExpressionEnvironment.STANDARD_CONVERSION_PROFILE_ID,
            new DefaultDataConversionService(),
            true);

    private final String profileId;
    private final DataConversionService dataConversionService;
    private final boolean deterministicForConstants;

    private BoundaryCoercion(
            String profileId,
            DataConversionService dataConversionService,
            boolean deterministicForConstants) {
        this.profileId = validateProfileId(profileId);
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService");
        this.deterministicForConstants = deterministicForConstants;
    }

    public static BoundaryCoercion standard() {
        return STANDARD;
    }

    public static BoundaryCoercion of(String profileId, DataConversionService dataConversionService) {
        return new BoundaryCoercion(profileId, dataConversionService, false);
    }

    public static BoundaryCoercion of(
            String profileId,
            DataConversionService dataConversionService,
            ConversionDeterminism determinism) {
        Objects.requireNonNull(determinism, "determinism");
        return new BoundaryCoercion(
                profileId,
                dataConversionService,
                determinism == ConversionDeterminism.DETERMINISTIC);
    }

    public BoundaryCoercion withProfileId(String profileId) {
        return new BoundaryCoercion(profileId, dataConversionService, deterministicForConstants);
    }

    public String profileId() {
        return profileId;
    }

    DataConversionService dataConversionService() {
        return dataConversionService;
    }

    public boolean deterministicForConstants() {
        return deterministicForConstants;
    }

    public boolean canConvert(Class<?> sourceType, ExpressionType targetType) {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(targetType, "targetType");
        return switch (targetType) {
            case ScalarType scalarType -> canConvertScalarType(sourceType, scalarType);
            case VectorType vectorType -> canConvertCollectionType(sourceType, vectorType.elementType());
            case CollectionType collectionType -> canConvertCollectionType(sourceType, collectionType.elementType());
            case MapType mapType -> canConvertMapType(sourceType, mapType.valueType());
            case UnknownType ignored -> true;
            case NullType ignored -> false;
            case ObjectType ignored -> false;
        };
    }

    public boolean canConvert(Object sourceValue, ExpressionType targetType) {
        Objects.requireNonNull(targetType, "targetType");
        try {
            convert(sourceValue, targetType);
            return true;
        } catch (BoundaryCoercionFailure exception) {
            // DataConversionService has no value-specific probe; a conversion rejection is the negative answer.
            return false;
        }
    }

    public boolean canConvert(ExpressionType sourceType, ExpressionType targetType) {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(targetType, "targetType");
        if (sourceType.equals(targetType) || targetType == UnknownType.INSTANCE) {
            return true;
        }
        if (sourceType == UnknownType.INSTANCE || sourceType == NullType.INSTANCE || targetType == NullType.INSTANCE) {
            return false;
        }
        return switch (sourceType) {
            case ScalarType scalarSource -> canConvertScalarSource(scalarSource, targetType);
            case VectorType vectorSource -> targetType instanceof VectorType vectorTarget
                    && canConvert(vectorSource.elementType(), vectorTarget.elementType());
            case CollectionType collectionSource -> targetType instanceof CollectionType collectionTarget
                    && canConvert(collectionSource.elementType(), collectionTarget.elementType());
            case MapType mapSource -> targetType instanceof MapType mapTarget
                    && canConvert(mapSource.valueType(), mapTarget.valueType());
            case ObjectType ignored -> false;
            case NullType ignored -> false;
            case UnknownType ignored -> false;
        };
    }

    Object convert(Object sourceValue, ExpressionType targetType) {
        return convertBoundaryValue("boundary value", sourceValue, targetType);
    }

    public Object convertFunctionBindingFallback(Object sourceValue, ExpressionType targetType) {
        return convertBoundaryValue("function-binding fallback value", sourceValue, targetType);
    }

    Object convertDefault(String symbolName, Object sourceValue, ExpressionType targetType) {
        return convertBoundaryValue("external symbol '" + symbolName + "' default", sourceValue, targetType);
    }

    private Object convertBoundaryValue(String boundaryName, Object sourceValue, ExpressionType targetType) {
        try {
            return convertValue(boundaryName, sourceValue, targetType);
        } catch (RuntimeException exception) {
            throw new BoundaryCoercionFailure(
                    boundaryName + " cannot be converted to " + targetType,
                    exception);
        }
    }

    private Object convertValue(String valueName, Object sourceValue, ExpressionType targetType) {
        Objects.requireNonNull(targetType, "targetType");
        return switch (targetType) {
            case ScalarType scalarType -> convertScalar(valueName, sourceValue, scalarType);
            case VectorType vectorType -> convertCollection(valueName, sourceValue, vectorType.elementType(), "VectorType");
            case CollectionType collectionType -> convertCollection(
                    valueName,
                    sourceValue,
                    collectionType.elementType(),
                    "CollectionType");
            case MapType mapType -> convertMap(valueName, sourceValue, mapType.valueType());
            case ObjectType ignored -> throw new IllegalArgumentException("object boundary coercion is not available");
            case NullType ignored -> {
                if (sourceValue != null) {
                    throw new IllegalArgumentException("null target requires a null value");
                }
                yield null;
            }
            case UnknownType ignored -> convertValue(valueName, sourceValue, ExternalSymbolDefaults.inferType(valueName, sourceValue));
        };
    }

    private Object convertScalar(String valueName, Object sourceValue, ScalarType scalarType) {
        if (sourceValue == null) {
            throw new IllegalArgumentException("scalar target requires a non-null value");
        }
        Class<?> targetClass = scalarJavaType(scalarType);
        if (targetClass.isInstance(sourceValue)) {
            return sourceValue;
        }
        Object converted = dataConversionService.convert(sourceValue, targetClass);
        if (converted == null || !targetClass.isInstance(converted)) {
            throw new IllegalArgumentException(
                    "conversion to " + scalarType + " returned " + (converted == null ? "null" : converted.getClass().getName()));
        }
        if (scalarType == ScalarType.NUMBER && converted instanceof BigDecimal number) {
            return number;
        }
        if (scalarType == ScalarType.NUMBER) {
            throw new IllegalArgumentException("numeric boundary coercion did not produce BigDecimal for " + valueName);
        }
        return converted;
    }

    private boolean canConvertScalarType(Class<?> sourceType, ScalarType scalarType) {
        Class<?> targetClass = scalarJavaType(scalarType);
        return targetClass.isAssignableFrom(sourceType) || dataConversionService.canConvert(sourceType, targetClass);
    }

    private boolean canConvertScalarSource(ScalarType sourceType, ExpressionType targetType) {
        if (!(targetType instanceof ScalarType scalarTarget)) {
            return false;
        }
        return canConvertScalarType(scalarJavaType(sourceType), scalarTarget);
    }

    private static boolean canConvertCollectionType(Class<?> sourceType, ExpressionType elementType) {
        return elementType == UnknownType.INSTANCE
                && (Collection.class.isAssignableFrom(sourceType) || sourceType.isArray());
    }

    private static boolean canConvertMapType(Class<?> sourceType, ExpressionType valueType) {
        return valueType == UnknownType.INSTANCE && Map.class.isAssignableFrom(sourceType);
    }

    private Object convertCollection(String valueName, Object sourceValue, ExpressionType elementType, String typeName) {
        if (sourceValue instanceof Collection<?> values) {
            ArrayList<Object> convertedValues = new ArrayList<>(values.size());
            for (Object element : values) {
                convertedValues.add(convertElement(valueName, elementType, element));
            }
            return Collections.unmodifiableList(convertedValues);
        }
        if (sourceValue != null && sourceValue.getClass().isArray()) {
            int length = Array.getLength(sourceValue);
            ArrayList<Object> convertedValues = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                convertedValues.add(convertElement(valueName, elementType, Array.get(sourceValue, index)));
            }
            return Collections.unmodifiableList(convertedValues);
        }
        throw new IllegalArgumentException("value must be a collection for " + typeName);
    }

    private Object convertElement(String valueName, ExpressionType elementType, Object element) {
        if (elementType == UnknownType.INSTANCE) {
            return convertValue(valueName, element, ExternalSymbolDefaults.inferType(valueName, element));
        }
        return convertValue(valueName, element, elementType);
    }

    private Map<String, Object> convertMap(String valueName, Object sourceValue, ExpressionType valueType) {
        if (!(sourceValue instanceof Map<?, ?> values)) {
            throw new IllegalArgumentException("value must be a map for MapType");
        }
        TreeMap<String, Object> sortedValues = new TreeMap<>();
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("MapType values must be text-keyed");
            }
            sortedValues.put(key, convertElement(valueName, valueType, entry.getValue()));
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(sortedValues));
    }

    private static Class<?> scalarJavaType(ScalarType scalarType) {
        return switch (scalarType) {
            case NUMBER -> BigDecimal.class;
            case BOOLEAN -> Boolean.class;
            case STRING -> String.class;
            case DATE -> LocalDate.class;
            case TIME -> LocalTime.class;
            case DATETIME -> LocalDateTime.class;
        };
    }

    static String validateProfileId(String profileId) {
        Objects.requireNonNull(profileId, "profileId");
        if (profileId.isBlank()) {
            throw new IllegalArgumentException("conversion profile id must not be blank");
        }
        return profileId;
    }

    private static final class BoundaryCoercionFailure extends IllegalArgumentException {

        private BoundaryCoercionFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
