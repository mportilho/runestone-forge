package com.runestone.converters.impl.stable;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConversionService;
import com.runestone.converters.DataConverter;
import com.runestone.converters.DataConverterConfigurationException;
import com.runestone.converters.NoDataConverterFoundException;
import com.runestone.converters.NonFoldableValueException;
import com.runestone.converters.StandardConverters;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class DefaultDataConversionService implements DataConversionService {

    private static final Pattern RULE_IDENTITY_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._:-]*");
    private static final List<String> BUILT_IN_CAPABILITIES = List.of(
            "array-to-array",
            "array-to-list",
            "collection-to-array",
            "enum-by-name-or-ordinal",
            "foldable-identity",
            "primitive-target-aliases");

    private static final ClassValue<EnumMetadata<?>> ENUM_METADATA = new ClassValue<>() {
        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected EnumMetadata<?> computeValue(Class<?> type) {
            return new EnumMetadata(type);
        }
    };

    private final ConversionContext context;
    private final Map<Class<?>, Map<Class<?>, DataConverter<?, ?>>> exactConverters;
    private final List<ConverterEntry> converters;
    private final String profileIdentity;
    private final String profileHash;
    private final ClassValue<Map<Class<?>, ConverterLookup>> assignableConverters = new ClassValue<>() {
        @Override
        protected Map<Class<?>, ConverterLookup> computeValue(Class<?> sourceType) {
            return findAssignableConverters(sourceType);
        }
    };

    private DefaultDataConversionService(
            ConversionContext context,
            Map<Class<?>, Map<Class<?>, DataConverter<?, ?>>> exactConverters,
            List<ConverterEntry> converters) {
        this.context = context;
        this.exactConverters = exactConverters;
        this.converters = converters;
        this.profileIdentity = createProfileIdentity(context, converters);
        this.profileHash = sha256(profileIdentity);
    }

    public static DefaultDataConversionService standard() {
        return withConverters(ConversionContext.standard(), StandardConverters.all());
    }

    public static DefaultDataConversionService withConverters(
            ConversionContext context,
            Collection<? extends DataConverter<?, ?>> converters) {
        Objects.requireNonNull(context, "Conversion context must be provided");
        Objects.requireNonNull(converters, "Converters must be provided");

        Map<Class<?>, Map<Class<?>, DataConverter<?, ?>>> exactConverters = new IdentityHashMap<>();
        List<ConverterEntry> validatedConverters = new ArrayList<>(converters.size());
        for (DataConverter<?, ?> converter : converters) {
            validateConverter(converter);
            Class<?> sourceType = converter.sourceType();
            Class<?> targetType = boxedType(converter.targetType());
            String ruleIdentity = converter.ruleIdentity();
            DataConverter<?, ?> duplicate = exactConverters
                    .computeIfAbsent(targetType, ignored -> new IdentityHashMap<>())
                    .putIfAbsent(sourceType, converter);
            if (duplicate != null) {
                throw new DataConverterConfigurationException("Duplicate converter for "
                        + sourceType.getName() + " -> " + targetType.getName());
            }
            validatedConverters.add(new ConverterEntry(sourceType, targetType, ruleIdentity, converter));
        }
        validatedConverters.sort(DefaultDataConversionService::compareConverters);
        return new DefaultDataConversionService(
                context,
                immutableExactConverters(exactConverters),
                List.copyOf(validatedConverters));
    }

    @Override
    public ConversionContext conversionContext() {
        return context;
    }

    @Override
    public String conversionProfileIdentity() {
        return profileIdentity;
    }

    @Override
    public String conversionProfileHash() {
        return profileHash;
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == null || targetType == null) {
            return false;
        }
        Class<?> boxedTargetType = boxedType(targetType);
        if (findExactConverter(sourceType, boxedTargetType) != null) {
            return isFoldableTargetType(targetType);
        }
        if (isContainerConversion(sourceType, targetType) || isEnumConversion(sourceType, targetType)) {
            return canConvertContainer(sourceType, targetType);
        }
        if (targetType.isAssignableFrom(sourceType) && canCopyIdentityResult(sourceType, targetType)) {
            return true;
        }
        return findConverter(sourceType, boxedTargetType) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> T convert(S source, Class<T> targetType) {
        Objects.requireNonNull(targetType, "Target Type must be provided");
        if (source == null) {
            return null;
        }

        Object converted;
        Class<?> boxedTargetType = boxedType(targetType);
        DataConverter<S, ?> exactConverter = (DataConverter<S, ?>) findExactConverter(source.getClass(), boxedTargetType);
        if (exactConverter != null) {
            converted = exactConverter.convert(source, context);
        } else if (source instanceof Collection<?> collection && targetType.isArray()) {
            converted = collectionToArray(collection, targetType.getComponentType());
        } else if (source.getClass().isArray() && targetType.isArray()) {
            converted = arrayToArray(source, targetType.getComponentType());
        } else if (source.getClass().isArray() && targetType == List.class) {
            converted = arrayToList(source);
        } else if (targetType.isEnum() && (source instanceof String || source instanceof Number)) {
            converted = convertEnum(source, targetType);
        } else if (targetType.isInstance(source) && canCopyIdentityResult(source.getClass(), targetType)) {
            converted = source;
        } else {
            DataConverter<S, ?> converter = (DataConverter<S, ?>) findConverter(source.getClass(), boxedTargetType);
            if (converter == null) {
                throw new NoDataConverterFoundException(source.getClass(), targetType);
            }
            converted = converter.convert(source, context);
        }

        if (converted == null) {
            throw new NoDataConverterFoundException(source.getClass(), targetType);
        }
        return (T) copyFoldableValue(converted);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T copyFoldableValue(T value) {
        return (T) copyValue(value);
    }

    private Object collectionToArray(Collection<?> source, Class<?> componentType) {
        if (!componentType.isPrimitive() && !isFoldableTargetType(componentType)) {
            throw new NonFoldableValueException("Array component type is not foldable: " + componentType.getName());
        }
        Object target = Array.newInstance(componentType, source.size());
        int index = 0;
        for (Object element : source) {
            if (element == null) {
                throw new NonFoldableValueException("Collections containing null elements are not foldable");
            }
            Array.set(target, index++, convert(element, boxedType(componentType)));
        }
        return target;
    }

    private Object arrayToArray(Object source, Class<?> componentType) {
        int length = Array.getLength(source);
        if (!componentType.isPrimitive() && !isFoldableTargetType(componentType)) {
            throw new NonFoldableValueException("Array component type is not foldable: " + componentType.getName());
        }
        Object target = Array.newInstance(componentType, length);
        for (int index = 0; index < length; index++) {
            Object element = Array.get(source, index);
            if (element == null) {
                if (componentType.isPrimitive()) {
                    throw new NonFoldableValueException("Null cannot be stored in an array of " + componentType.getName());
                }
                Array.set(target, index, null);
            } else {
                Array.set(target, index, convert(element, boxedType(componentType)));
            }
        }
        return target;
    }

    private List<?> arrayToList(Object source) {
        int length = Array.getLength(source);
        List<Object> values = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            Object element = Array.get(source, index);
            if (element == null) {
                throw new NonFoldableValueException("Arrays containing null elements cannot be converted to a foldable list");
            }
            values.add(copyValue(element));
        }
        return List.copyOf(values);
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertEnum(Object source, Class<T> targetType) {
        EnumMetadata<T> metadata = (EnumMetadata<T>) ENUM_METADATA.get(targetType);
        if (source instanceof String name) {
            return metadata.byName(name);
        }
        Integer ordinal = enumOrdinal((Number) source);
        return ordinal == null ? null : metadata.byOrdinal(ordinal);
    }

    private DataConverter<?, ?> findConverter(Class<?> sourceType, Class<?> targetType) {
        DataConverter<?, ?> exact = findExactConverter(sourceType, targetType);
        if (exact != null) {
            return exact;
        }

        ConverterLookup lookup = assignableConverters.get(sourceType).get(targetType);
        return lookup == null ? null : lookup.resolve(sourceType, targetType);
    }

    private DataConverter<?, ?> findExactConverter(Class<?> sourceType, Class<?> targetType) {
        Map<Class<?>, DataConverter<?, ?>> sourceConverters = exactConverters.get(targetType);
        return sourceConverters == null ? null : sourceConverters.get(sourceType);
    }

    private Map<Class<?>, ConverterLookup> findAssignableConverters(Class<?> sourceType) {
        Map<Class<?>, ConverterCandidate> selectedConverters = new IdentityHashMap<>();
        for (ConverterEntry converter : converters) {
            if (!converter.sourceType().isAssignableFrom(sourceType)) {
                continue;
            }
            int distance = typeDistance(sourceType, converter.sourceType());
            ConverterCandidate selected = selectedConverters.get(converter.targetType());
            if (selected == null || distance < selected.distance()) {
                selectedConverters.put(converter.targetType(), new ConverterCandidate(new ConverterFound(converter.converter()), distance));
            } else if (distance == selected.distance()) {
                selectedConverters.put(converter.targetType(), new ConverterCandidate(AmbiguousConverter.INSTANCE, distance));
            }
        }

        Map<Class<?>, ConverterLookup> resolvedConverters = new IdentityHashMap<>(selectedConverters.size());
        for (Map.Entry<Class<?>, ConverterCandidate> entry : selectedConverters.entrySet()) {
            ConverterCandidate candidate = entry.getValue();
            resolvedConverters.put(entry.getKey(), candidate.lookup());
        }
        return Map.copyOf(resolvedConverters);
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

    private static Object copyValue(Object value) {
        if (value == null) {
            return null;
        }
        if (isKnownImmutable(value)) {
            return value;
        }
        if (value instanceof Timestamp timestamp) {
            Timestamp copy = new Timestamp(timestamp.getTime());
            copy.setNanos(timestamp.getNanos());
            return copy;
        }
        if (value instanceof java.sql.Date date) {
            return new java.sql.Date(date.getTime());
        }
        if (value instanceof java.util.Date date) {
            return new java.util.Date(date.getTime());
        }
        if (value.getClass().isArray()) {
            return copyArray(value);
        }
        if (value instanceof Map<?, ?> map) {
            return copyMap(map);
        }
        if (value instanceof Set<?> set) {
            return copySet(set);
        }
        if (value instanceof Collection<?> collection) {
            return copyCollection(collection);
        }
        throw new NonFoldableValueException("Unsupported foldable value type: " + value.getClass().getName());
    }

    private static Object copyArray(Object source) {
        int length = Array.getLength(source);
        Class<?> componentType = source.getClass().getComponentType();
        Object copy = Array.newInstance(componentType, length);
        if (componentType.isPrimitive()) {
            System.arraycopy(source, 0, copy, 0, length);
            return copy;
        }
        for (int index = 0; index < length; index++) {
            Object element = Array.get(source, index);
            Array.set(copy, index, element == null ? null : copyValue(element));
        }
        return copy;
    }

    private static Map<?, ?> copyMap(Map<?, ?> source) {
        Map<Object, Object> copy = new LinkedHashMap<>(source.size());
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new NonFoldableValueException("Maps containing null keys or values are not foldable");
            }
            copy.put(copyValue(entry.getKey()), copyValue(entry.getValue()));
        }
        return copy;
    }

    private static Set<?> copySet(Set<?> source) {
        Set<Object> copy = new LinkedHashSet<>(source.size());
        for (Object element : source) {
            if (element == null) {
                throw new NonFoldableValueException("Sets containing null elements are not foldable");
            }
            copy.add(copyValue(element));
        }
        return copy;
    }

    private static List<?> copyCollection(Collection<?> source) {
        List<Object> copy = new ArrayList<>(source.size());
        for (Object element : source) {
            if (element == null) {
                throw new NonFoldableValueException("Collections containing null elements are not foldable");
            }
            copy.add(copyValue(element));
        }
        return copy;
    }

    private static boolean isKnownImmutable(Object value) {
        return value instanceof String
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Float
                || value instanceof Double
                || value instanceof BigInteger
                || value instanceof BigDecimal
                || value instanceof Enum<?>
                || value instanceof UUID
                || value instanceof URI
                || value instanceof Locale
                || value instanceof Currency
                || value instanceof Pattern
                || value instanceof Instant
                || value instanceof LocalDate
                || value instanceof LocalDateTime
                || value instanceof LocalTime
                || value instanceof OffsetDateTime
                || value instanceof OffsetTime
                || value instanceof Year
                || value instanceof YearMonth
                || value instanceof ZonedDateTime
                || value instanceof Duration
                || value instanceof Period
                || value instanceof ZoneId
                || value instanceof ZoneOffset;
    }

    private static boolean isSupportedFoldableType(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || type.isArray()
                || Collection.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type)
                || type == String.class
                || type == Boolean.class
                || type == Character.class
                || Number.class.isAssignableFrom(type)
                || type == UUID.class
                || type == URI.class
                || type == Locale.class
                || type == Currency.class
                || type == Pattern.class
                || type == Instant.class
                || type == LocalDate.class
                || type == LocalDateTime.class
                || type == LocalTime.class
                || type == OffsetDateTime.class
                || type == OffsetTime.class
                || type == Year.class
                || type == YearMonth.class
                || type == ZonedDateTime.class
                || type == Duration.class
                || type == Period.class
                || ZoneId.class.isAssignableFrom(type)
                || java.util.Date.class.isAssignableFrom(type);
    }

    private static boolean isFoldableTargetType(Class<?> targetType) {
        if (targetType.isPrimitive() || targetType.isEnum()) {
            return true;
        }
        if (targetType.isArray()) {
            return isFoldableTargetType(targetType.getComponentType());
        }
        if (Collection.class.isAssignableFrom(targetType)) {
            return targetType == Collection.class
                    || targetType == List.class
                    || targetType == Set.class
                    || targetType == ArrayList.class
                    || targetType == LinkedHashSet.class;
        }
        if (Map.class.isAssignableFrom(targetType)) {
            return targetType == Map.class || targetType == LinkedHashMap.class;
        }
        return isSupportedFoldableType(targetType);
    }

    private static boolean canCopyIdentityResult(Class<?> sourceType, Class<?> targetType) {
        if (sourceType.isPrimitive() || sourceType.isEnum() || isKnownImmutableType(sourceType)) {
            return true;
        }
        if (Timestamp.class.isAssignableFrom(sourceType)) {
            return targetType.isAssignableFrom(Timestamp.class);
        }
        if (java.sql.Date.class.isAssignableFrom(sourceType)) {
            return targetType.isAssignableFrom(java.sql.Date.class);
        }
        if (java.util.Date.class.isAssignableFrom(sourceType)) {
            return targetType.isAssignableFrom(java.util.Date.class);
        }
        if (Set.class.isAssignableFrom(sourceType)) {
            return targetType == Object.class || targetType == Set.class || targetType == Collection.class;
        }
        if (Collection.class.isAssignableFrom(sourceType)) {
            return targetType == Object.class || targetType == List.class || targetType == Collection.class;
        }
        if (Map.class.isAssignableFrom(sourceType)) {
            return targetType == Object.class || targetType == Map.class;
        }
        return false;
    }

    private static boolean isKnownImmutableType(Class<?> type) {
        return type == String.class
                || type == Boolean.class
                || type == Character.class
                || type == Byte.class
                || type == Short.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class
                || type == BigInteger.class
                || type == BigDecimal.class
                || type == UUID.class
                || type == URI.class
                || type == Locale.class
                || type == Currency.class
                || type == Pattern.class
                || type == Instant.class
                || type == LocalDate.class
                || type == LocalDateTime.class
                || type == LocalTime.class
                || type == OffsetDateTime.class
                || type == OffsetTime.class
                || type == Year.class
                || type == YearMonth.class
                || type == ZonedDateTime.class
                || type == Duration.class
                || type == Period.class
                || type == ZoneId.class
                || type == ZoneOffset.class;
    }

    private static boolean isContainerConversion(Class<?> sourceType, Class<?> targetType) {
        return targetType.isArray() && (sourceType.isArray() || Collection.class.isAssignableFrom(sourceType))
                || sourceType.isArray() && targetType == List.class;
    }

    private boolean canConvertContainer(Class<?> sourceType, Class<?> targetType) {
        if (sourceType.isArray() && targetType.isArray()) {
            return canConvertArrayComponent(sourceType.getComponentType(), targetType.getComponentType());
        }
        if (sourceType.isArray() && targetType == List.class) {
            Class<?> componentType = sourceType.getComponentType();
            return componentType.isPrimitive() || isFoldableTargetType(componentType);
        }
        if (Collection.class.isAssignableFrom(sourceType) && targetType.isArray()) {
            Class<?> componentType = targetType.getComponentType();
            return componentType.isPrimitive() || isFoldableTargetType(componentType);
        }
        return isEnumConversion(sourceType, targetType);
    }

    private boolean canConvertArrayComponent(Class<?> sourceComponentType, Class<?> targetComponentType) {
        if (sourceComponentType.isArray() && targetComponentType.isArray()) {
            return canConvertContainer(sourceComponentType, targetComponentType);
        }
        if (targetComponentType.isPrimitive()) {
            Class<?> boxedSourceComponentType = boxedType(sourceComponentType);
            return sourceComponentType.isPrimitive() && sourceComponentType == targetComponentType
                    || canConvert(boxedSourceComponentType, boxedType(targetComponentType));
        }
        if (sourceComponentType.isPrimitive()) {
            Class<?> boxedSourceComponentType = boxedType(sourceComponentType);
            return targetComponentType.isAssignableFrom(boxedSourceComponentType)
                    || findConverter(boxedSourceComponentType, boxedType(targetComponentType)) != null;
        }
        return targetComponentType.isAssignableFrom(sourceComponentType) && isSupportedFoldableType(sourceComponentType)
                || findConverter(sourceComponentType, boxedType(targetComponentType)) != null;
    }

    private static boolean isEnumConversion(Class<?> sourceType, Class<?> targetType) {
        return targetType.isEnum()
                && (String.class.isAssignableFrom(sourceType) || Number.class.isAssignableFrom(sourceType));
    }

    private static void validateConverter(DataConverter<?, ?> converter) {
        if (converter == null) {
            throw new DataConverterConfigurationException("Converter must not be null");
        }
        if (converter.sourceType() == null) {
            throw new DataConverterConfigurationException("Converter source type must not be null");
        }
        if (converter.targetType() == null) {
            throw new DataConverterConfigurationException("Converter target type must not be null");
        }
        String identity = converter.ruleIdentity();
        if (identity == null || !RULE_IDENTITY_PATTERN.matcher(identity).matches()) {
            throw new DataConverterConfigurationException("Invalid converter rule identity: " + identity);
        }
    }

    private static int compareConverters(ConverterEntry first, ConverterEntry second) {
        int sourceComparison = first.sourceType().getName().compareTo(second.sourceType().getName());
        if (sourceComparison != 0) {
            return sourceComparison;
        }
        int targetComparison = boxedType(first.targetType()).getName().compareTo(boxedType(second.targetType()).getName());
        if (targetComparison != 0) {
            return targetComparison;
        }
        return first.ruleIdentity().compareTo(second.ruleIdentity());
    }

    private static String createProfileIdentity(ConversionContext context, List<ConverterEntry> converters) {
        StringBuilder identity = new StringBuilder("foldable-conversion-v1\n")
                .append("zone=").append(context.zoneId().getId()).append('\n')
                .append("locale=").append(context.locale().toLanguageTag()).append('\n');
        for (String capability : BUILT_IN_CAPABILITIES) {
            identity.append("capability=").append(capability).append('\n');
        }
        for (ConverterEntry converter : converters) {
            identity.append("rule=")
                    .append(converter.sourceType().getName())
                    .append("->")
                    .append(boxedType(converter.targetType()).getName())
                    .append('#')
                    .append(converter.ruleIdentity())
                    .append('\n');
        }
        return identity.toString();
    }

    private static Map<Class<?>, Map<Class<?>, DataConverter<?, ?>>> immutableExactConverters(
            Map<Class<?>, Map<Class<?>, DataConverter<?, ?>>> converters) {
        Map<Class<?>, Map<Class<?>, DataConverter<?, ?>>> copiedConverters = new IdentityHashMap<>(converters.size());
        for (Map.Entry<Class<?>, Map<Class<?>, DataConverter<?, ?>>> entry : converters.entrySet()) {
            copiedConverters.put(entry.getKey(), Collections.unmodifiableMap(new IdentityHashMap<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copiedConverters);
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
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
            String ruleIdentity,
            DataConverter<?, ?> converter) {
    }

    private record ConverterCandidate(ConverterLookup lookup, int distance) {

        private ConverterCandidate {
            Objects.requireNonNull(lookup, "Converter lookup must be provided");
        }
    }

    private sealed interface ConverterLookup permits ConverterFound, AmbiguousConverter {

        DataConverter<?, ?> resolve(Class<?> sourceType, Class<?> targetType);
    }

    private record ConverterFound(DataConverter<?, ?> converter) implements ConverterLookup {

        private ConverterFound {
            Objects.requireNonNull(converter, "Converter must be provided");
        }

        @Override
        public DataConverter<?, ?> resolve(Class<?> sourceType, Class<?> targetType) {
            return converter;
        }
    }

    private enum AmbiguousConverter implements ConverterLookup {
        INSTANCE;

        @Override
        public DataConverter<?, ?> resolve(Class<?> sourceType, Class<?> targetType) {
            throw new DataConverterConfigurationException("Ambiguous converters for "
                    + sourceType.getName() + " -> " + targetType.getName());
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
