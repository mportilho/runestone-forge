package com.runestone.expeval_mk3.api;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;

import java.lang.reflect.Array;
import java.math.BigDecimal;
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
            ExpressionEnvironment.STANDARD_CONVERSION_PROFILE_IDENTITY,
            new DefaultDataConversionService());

    private final String profileIdentity;
    private final DataConversionService dataConversionService;

    private BoundaryCoercion(String profileIdentity, DataConversionService dataConversionService) {
        this.profileIdentity = validateProfileIdentity(profileIdentity);
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService");
    }

    public static BoundaryCoercion standard() {
        return STANDARD;
    }

    public static BoundaryCoercion of(String profileIdentity, DataConversionService dataConversionService) {
        return new BoundaryCoercion(profileIdentity, dataConversionService);
    }

    public BoundaryCoercion withProfileIdentity(String profileIdentity) {
        return new BoundaryCoercion(profileIdentity, dataConversionService);
    }

    public String profileIdentity() {
        return profileIdentity;
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

    public boolean canConvert(ExpressionType sourceType, ExpressionType targetType) {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(targetType, "targetType");
        if (sourceType.equals(targetType) || sourceType == UnknownType.INSTANCE || targetType == UnknownType.INSTANCE) {
            return true;
        }
        return switch (sourceType) {
            case ScalarType scalarType -> canConvert(ExpressionJavaTypes.scalarValueType(scalarType), targetType);
            case VectorType vectorType -> canConvertCollectionExpressionType(vectorType.elementType(), targetType);
            case CollectionType collectionType -> canConvertCollectionExpressionType(collectionType.elementType(), targetType);
            case MapType mapType -> canConvertMapExpressionType(mapType.valueType(), targetType);
            case NullType ignored -> false;
            case UnknownType ignored -> true;
            case ObjectType ignored -> false;
        };
    }

    public boolean canConvert(Object sourceValue, ExpressionType targetType) {
        Objects.requireNonNull(targetType, "targetType");
        try {
            convertBoundaryValue("boundary value", sourceValue, targetType);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public Object convertFunctionBindingFallback(Object sourceValue, ExpressionType targetType) {
        Objects.requireNonNull(targetType, "targetType");
        return convertBoundaryValue("function-binding fallback value", sourceValue, targetType);
    }

    Object convertDefault(String symbolName, Object sourceValue, ExpressionType targetType) {
        return convertBoundaryValue("external symbol '" + symbolName + "' default", sourceValue, targetType);
    }

    private Object convertBoundaryValue(String boundaryName, Object sourceValue, ExpressionType targetType) {
        try {
            return convertValue(boundaryName, sourceValue, targetType);
        } catch (RuntimeException exception) {
            throw new BoundaryCoercionFailure(boundaryName + " cannot be converted to " + targetType, exception);
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
            case UnknownType ignored -> convertValue(valueName, sourceValue,
                    ExternalSymbolDefaults.inferType(valueName, sourceValue));
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
            return elementType == UnknownType.INSTANCE;
        }
        if (!sourceType.isArray()) {
            return false;
        }
        return elementType == UnknownType.INSTANCE || canConvert(sourceType.getComponentType(), elementType);
    }

    private static boolean canConvertMapType(Class<?> sourceType, ExpressionType valueType) {
        return valueType == UnknownType.INSTANCE && Map.class.isAssignableFrom(sourceType);
    }

    private boolean canConvertCollectionExpressionType(ExpressionType sourceElementType, ExpressionType targetType) {
        return switch (targetType) {
            case VectorType vectorType -> canConvert(sourceElementType, vectorType.elementType());
            case CollectionType collectionType -> canConvert(sourceElementType, collectionType.elementType());
            case UnknownType ignored -> true;
            case ScalarType ignored -> false;
            case MapType ignored -> false;
            case ObjectType ignored -> false;
            case NullType ignored -> false;
        };
    }

    private boolean canConvertMapExpressionType(ExpressionType sourceValueType, ExpressionType targetType) {
        return switch (targetType) {
            case MapType mapType -> canConvert(sourceValueType, mapType.valueType());
            case UnknownType ignored -> true;
            case ScalarType ignored -> false;
            case VectorType ignored -> false;
            case CollectionType ignored -> false;
            case ObjectType ignored -> false;
            case NullType ignored -> false;
        };
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

    static String validateProfileIdentity(String profileIdentity) {
        Objects.requireNonNull(profileIdentity, "profileIdentity");
        if (profileIdentity.isBlank()) {
            throw new IllegalArgumentException("conversion profile identity must not be blank");
        }
        return profileIdentity;
    }

    private static final class BoundaryCoercionFailure extends IllegalArgumentException {

        private BoundaryCoercionFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
