package com.runestone.converters.impl.runtime;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConverterConfigurationException;
import com.runestone.converters.NoDataConverterFoundException;
import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.converters.RuntimeDataConverter;
import com.runestone.converters.RuntimeStandardConverters;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public final class DefaultRuntimeDataConversionService implements RuntimeDataConversionService {

    private static final ClassValue<EnumMetadata<?>> ENUM_METADATA = new ClassValue<>() {
        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected EnumMetadata<?> computeValue(Class<?> type) {
            return new EnumMetadata(type);
        }
    };

    private final ConversionContext context;
    private final Map<Class<?>, Map<Class<?>, RuntimeDataConverter<?, ?>>> exactConverters;
    private final List<ConverterEntry> converters;
    private final ClassValue<Map<Class<?>, ConverterLookup>> assignableConverters = new ClassValue<>() {
        @Override
        protected Map<Class<?>, ConverterLookup> computeValue(Class<?> sourceType) {
            return findAssignableConverters(sourceType);
        }
    };

    private DefaultRuntimeDataConversionService(
            ConversionContext context,
            Map<Class<?>, Map<Class<?>, RuntimeDataConverter<?, ?>>> exactConverters,
            List<ConverterEntry> converters) {
        this.context = context;
        this.exactConverters = exactConverters;
        this.converters = converters;
    }

    public static DefaultRuntimeDataConversionService standard() {
        return withConverters(ConversionContext.system(), RuntimeStandardConverters.all());
    }

    public static DefaultRuntimeDataConversionService withConverters(
            ConversionContext context,
            Collection<? extends RuntimeDataConverter<?, ?>> converters) {
        Objects.requireNonNull(context, "Conversion context must be provided");
        Objects.requireNonNull(converters, "Converters must be provided");

        Map<Class<?>, Map<Class<?>, RuntimeDataConverter<?, ?>>> exactConverters = new IdentityHashMap<>();
        List<ConverterEntry> validatedConverters = new ArrayList<>(converters.size());
        for (RuntimeDataConverter<?, ?> converter : converters) {
            validateConverter(converter);
            Class<?> sourceType = boxedType(converter.sourceType());
            Class<?> targetType = boxedType(converter.targetType());
            RuntimeDataConverter<?, ?> duplicate = exactConverters
                    .computeIfAbsent(targetType, ignored -> new IdentityHashMap<>())
                    .putIfAbsent(sourceType, converter);
            if (duplicate != null) {
                throw new DataConverterConfigurationException("Duplicate runtime converter for "
                        + sourceType.getName() + " -> " + targetType.getName());
            }
            validatedConverters.add(new ConverterEntry(sourceType, targetType, converter));
        }

        return new DefaultRuntimeDataConversionService(
                context,
                immutableExactConverters(exactConverters),
                List.copyOf(validatedConverters));
    }

    @Override
    public ConversionContext conversionContext() {
        return context;
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == null || targetType == null) {
            return false;
        }
        Class<?> boxedSourceType = boxedType(sourceType);
        Class<?> boxedTargetType = boxedType(targetType);
        if (boxedTargetType.isAssignableFrom(boxedSourceType)) {
            return true;
        }
        if (isContainerConversion(sourceType, targetType) || isEnumConversion(boxedSourceType, boxedTargetType)) {
            return canConvertContainer(sourceType, targetType);
        }
        return findConverter(boxedSourceType, boxedTargetType) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> T convert(S source, Class<T> targetType) {
        Objects.requireNonNull(targetType, "Target Type must be provided");
        if (source == null) {
            return null;
        }

        Class<?> boxedTargetType = boxedType(targetType);
        Object converted;
        if (boxedTargetType.isInstance(source)) {
            converted = source;
        } else if (source instanceof Collection<?> collection && targetType.isArray()) {
            converted = collectionToArray(collection, targetType.getComponentType());
        } else if (source.getClass().isArray() && targetType.isArray()) {
            converted = arrayToArray(source, targetType.getComponentType());
        } else if (source.getClass().isArray() && targetType == List.class) {
            converted = arrayToList(source);
        } else if (targetType.isEnum() && (source instanceof String || source instanceof Number)) {
            converted = convertEnum(source, targetType);
        } else {
            RuntimeDataConverter<S, ?> converter = (RuntimeDataConverter<S, ?>) findConverter(source.getClass(), boxedTargetType);
            if (converter == null) {
                throw new NoDataConverterFoundException(source.getClass(), targetType);
            }
            converted = converter.convert(source, context);
        }

        if (converted == null) {
            throw new DataConverterConfigurationException("Runtime converter returned null for "
                    + source.getClass().getName() + " -> " + boxedTargetType.getName());
        }
        return (T) converted;
    }

    private Object collectionToArray(Collection<?> source, Class<?> componentType) {
        Object target = Array.newInstance(componentType, source.size());
        int index = 0;
        for (Object element : source) {
            if (element == null && componentType.isPrimitive()) {
                throw new DataConverterConfigurationException("Null cannot be stored in an array of " + componentType.getName());
            }
            Array.set(target, index++, convert(element, boxedType(componentType)));
        }
        return target;
    }

    private Object arrayToArray(Object source, Class<?> componentType) {
        int length = Array.getLength(source);
        Object target = Array.newInstance(componentType, length);
        for (int index = 0; index < length; index++) {
            Object element = Array.get(source, index);
            if (element == null && componentType.isPrimitive()) {
                throw new DataConverterConfigurationException("Null cannot be stored in an array of " + componentType.getName());
            }
            Array.set(target, index, convert(element, boxedType(componentType)));
        }
        return target;
    }

    private static List<?> arrayToList(Object source) {
        if (source instanceof Object[] values) {
            return Arrays.asList(values);
        }
        int length = Array.getLength(source);
        List<Object> values = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            values.add(Array.get(source, index));
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertEnum(Object source, Class<T> targetType) {
        EnumMetadata<T> metadata = (EnumMetadata<T>) ENUM_METADATA.get(targetType);
        T converted;
        if (source instanceof String name) {
            converted = metadata.byName(name);
        } else {
            Integer ordinal = enumOrdinal((Number) source);
            converted = ordinal == null ? null : metadata.byOrdinal(ordinal);
        }
        if (converted == null) {
            throw new NoDataConverterFoundException(source.getClass(), targetType);
        }
        return converted;
    }

    private RuntimeDataConverter<?, ?> findConverter(Class<?> sourceType, Class<?> targetType) {
        Class<?> boxedSourceType = boxedType(sourceType);
        Class<?> boxedTargetType = boxedType(targetType);
        RuntimeDataConverter<?, ?> exact = findExactConverter(boxedSourceType, boxedTargetType);
        if (exact != null) {
            return exact;
        }

        ConverterLookup lookup = assignableConverters.get(boxedSourceType).get(boxedTargetType);
        return lookup == null ? null : lookup.resolve(boxedSourceType, boxedTargetType);
    }

    private RuntimeDataConverter<?, ?> findExactConverter(Class<?> sourceType, Class<?> targetType) {
        Map<Class<?>, RuntimeDataConverter<?, ?>> sourceConverters = exactConverters.get(targetType);
        return sourceConverters == null ? null : sourceConverters.get(sourceType);
    }

    private Map<Class<?>, ConverterLookup> findAssignableConverters(Class<?> sourceType) {
        Map<Class<?>, ConverterCandidate> selectedConverters = new HashMap<>();
        for (ConverterEntry converter : converters) {
            if (!converter.sourceType().isAssignableFrom(sourceType)) {
                continue;
            }
            int distance = typeDistance(sourceType, converter.sourceType());
            ConverterCandidate selected = selectedConverters.get(converter.targetType());
            if (selected == null || distance < selected.distance()) {
                selectedConverters.put(converter.targetType(), new ConverterCandidate(converter.converter(), distance, false));
            } else if (distance == selected.distance()) {
                selectedConverters.put(converter.targetType(), new ConverterCandidate(null, distance, true));
            }
        }

        Map<Class<?>, ConverterLookup> resolvedConverters = new HashMap<>(selectedConverters.size());
        for (Map.Entry<Class<?>, ConverterCandidate> entry : selectedConverters.entrySet()) {
            ConverterCandidate candidate = entry.getValue();
            resolvedConverters.put(entry.getKey(), new ConverterLookup(candidate.converter(), candidate.ambiguous()));
        }
        return Map.copyOf(resolvedConverters);
    }

    private boolean canConvertContainer(Class<?> sourceType, Class<?> targetType) {
        if (sourceType.isArray() && targetType.isArray()) {
            return canConvertArrayComponent(sourceType.getComponentType(), targetType.getComponentType());
        }
        if (sourceType.isArray() && targetType == List.class) {
            return true;
        }
        if (Collection.class.isAssignableFrom(sourceType) && targetType.isArray()) {
            return true;
        }
        return isEnumConversion(boxedType(sourceType), boxedType(targetType));
    }

    private boolean canConvertArrayComponent(Class<?> sourceComponentType, Class<?> targetComponentType) {
        if (sourceComponentType.isArray() && targetComponentType.isArray()) {
            return canConvertContainer(sourceComponentType, targetComponentType);
        }
        Class<?> boxedSourceComponentType = boxedType(sourceComponentType);
        Class<?> boxedTargetComponentType = boxedType(targetComponentType);
        return boxedTargetComponentType.isAssignableFrom(boxedSourceComponentType)
                || isEnumConversion(boxedSourceComponentType, boxedTargetComponentType)
                || findConverter(boxedSourceComponentType, boxedTargetComponentType) != null;
    }

    private static boolean isContainerConversion(Class<?> sourceType, Class<?> targetType) {
        return targetType.isArray() && (sourceType.isArray() || Collection.class.isAssignableFrom(sourceType))
                || sourceType.isArray() && targetType == List.class;
    }

    private static boolean isEnumConversion(Class<?> sourceType, Class<?> targetType) {
        return targetType.isEnum()
                && (String.class.isAssignableFrom(sourceType) || Number.class.isAssignableFrom(sourceType));
    }

    private static int typeDistance(Class<?> sourceType, Class<?> declaredSourceType) {
        Queue<TypeNode> pending = new ArrayDeque<>();
        Set<Class<?>> visited = new HashSet<>();
        pending.add(new TypeNode(sourceType, 0));
        while (!pending.isEmpty()) {
            TypeNode current = pending.remove();
            if (!visited.add(current.type())) {
                continue;
            }
            if (current.type() == declaredSourceType) {
                return current.distance();
            }
            Class<?> superclass = current.type().getSuperclass();
            if (superclass != null) {
                pending.add(new TypeNode(superclass, current.distance() + 1));
            }
            for (Class<?> interfaceType : current.type().getInterfaces()) {
                pending.add(new TypeNode(interfaceType, current.distance() + 1));
            }
        }
        return Integer.MAX_VALUE;
    }

    private static void validateConverter(RuntimeDataConverter<?, ?> converter) {
        if (converter == null) {
            throw new DataConverterConfigurationException("Runtime converter must not be null");
        }
        if (converter.sourceType() == null) {
            throw new DataConverterConfigurationException("Runtime converter source type must not be null");
        }
        if (converter.targetType() == null) {
            throw new DataConverterConfigurationException("Runtime converter target type must not be null");
        }
    }

    private static Map<Class<?>, Map<Class<?>, RuntimeDataConverter<?, ?>>> immutableExactConverters(
            Map<Class<?>, Map<Class<?>, RuntimeDataConverter<?, ?>>> converters) {
        Map<Class<?>, Map<Class<?>, RuntimeDataConverter<?, ?>>> copiedConverters = new IdentityHashMap<>(converters.size());
        for (Map.Entry<Class<?>, Map<Class<?>, RuntimeDataConverter<?, ?>>> entry : converters.entrySet()) {
            copiedConverters.put(entry.getKey(), Collections.unmodifiableMap(new IdentityHashMap<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copiedConverters);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> boxedType(Class<T> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return (Class<T>) switch (type.getName()) {
            case "boolean" -> Boolean.class;
            case "byte" -> Byte.class;
            case "char" -> Character.class;
            case "short" -> Short.class;
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "float" -> Float.class;
            case "double" -> Double.class;
            default -> type;
        };
    }

    private static Integer enumOrdinal(Number value) {
        return switch (value) {
            case Byte byteValue -> byteValue.intValue();
            case Short shortValue -> shortValue.intValue();
            case Integer integer -> integer;
            case Long longValue when longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE -> longValue.intValue();
            case BigInteger bigInteger when bigInteger.bitLength() < 32 -> bigInteger.intValue();
            case BigDecimal bigDecimal -> exactOrdinal(bigDecimal);
            case Float floatValue when Float.isFinite(floatValue) && Math.rint(floatValue) == floatValue -> boundedOrdinal(floatValue.longValue());
            case Double doubleValue when Double.isFinite(doubleValue) && Math.rint(doubleValue) == doubleValue -> boundedOrdinal(doubleValue.longValue());
            default -> null;
        };
    }

    private static Integer exactOrdinal(BigDecimal value) {
        try {
            return boundedOrdinal(value.longValueExact());
        } catch (ArithmeticException exception) {
            return null;
        }
    }

    private static Integer boundedOrdinal(long value) {
        return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE ? (int) value : null;
    }

    private record TypeNode(Class<?> type, int distance) {
    }

    private record ConverterEntry(
            Class<?> sourceType,
            Class<?> targetType,
            RuntimeDataConverter<?, ?> converter) {
    }

    private record ConverterCandidate(RuntimeDataConverter<?, ?> converter, int distance, boolean ambiguous) {
    }

    private record ConverterLookup(RuntimeDataConverter<?, ?> converter, boolean ambiguous) {

        private RuntimeDataConverter<?, ?> resolve(Class<?> sourceType, Class<?> targetType) {
            if (ambiguous) {
                throw new DataConverterConfigurationException("Ambiguous runtime converters for "
                        + sourceType.getName() + " -> " + targetType.getName());
            }
            return converter;
        }
    }

    private static final class EnumMetadata<T> {
        private final T[] constants;
        private final Map<String, T> byName;

        private EnumMetadata(Class<T> enumType) {
            this.constants = enumType.getEnumConstants();
            this.byName = new HashMap<>(constants.length);
            for (T constant : constants) {
                byName.put(((Enum<?>) constant).name().toUpperCase(Locale.ROOT), constant);
            }
        }

        private T byName(String name) {
            return byName.get(name.toUpperCase(Locale.ROOT));
        }

        private T byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal < constants.length ? constants[ordinal] : null;
        }
    }
}
