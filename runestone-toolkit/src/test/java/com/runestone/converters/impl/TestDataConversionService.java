package com.runestone.converters.impl;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConversionService;
import com.runestone.converters.DataConverter;
import com.runestone.converters.DataConverterConfigurationException;
import com.runestone.converters.NoDataConverterFoundException;
import com.runestone.converters.NonFoldableValueException;
import com.runestone.converters.StandardConverters;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDataConversionService {

    @Test
    void standardServiceHasDeterministicContextAndProfile() throws Exception {
        DataConversionService service = DefaultDataConversionService.standard();

        assertThat(service.conversionContext().zoneId()).isEqualTo(ZoneId.of("UTC"));
        assertThat(service.conversionContext().locale()).isEqualTo(Locale.ROOT);
        assertThat(service.conversionProfileIdentity()).contains("zone=UTC\nlocale=und");
        assertThat(service.conversionProfileHash()).isEqualTo(sha256(service.conversionProfileIdentity()));
    }

    @Test
    void profileDoesNotDependOnConverterRegistrationOrder() {
        DataConverter<String, Integer> stringToInteger = rule(String.class, Integer.class, "test.string.integer", (source, context) -> 1);
        DataConverter<Number, String> numberToString = rule(Number.class, String.class, "test.number.string", (source, context) -> "number");

        DataConversionService first = DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(stringToInteger, numberToString));
        DataConversionService second = DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(numberToString, stringToInteger));

        assertThat(first.conversionProfileIdentity()).isEqualTo(second.conversionProfileIdentity());
        assertThat(first.conversionProfileHash()).isEqualTo(second.conversionProfileHash());
    }

    @Test
    void rejectsInvalidAndDuplicateConverterDefinitions() {
        DataConverter<String, Integer> valid = rule(String.class, Integer.class, "test.valid", (source, context) -> 1);

        assertThatThrownBy(() -> DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                valid,
                rule(String.class, Integer.class, "test.duplicate", (source, context) -> 2))))
                .isInstanceOf(DataConverterConfigurationException.class);
        assertThatThrownBy(() -> DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(String.class, Integer.class, "Invalid identity", (source, context) -> 1))))
                .isInstanceOf(DataConverterConfigurationException.class);
        assertThatThrownBy(() -> DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(null, Integer.class, "test.missing-source", (source, context) -> 1))))
                .isInstanceOf(DataConverterConfigurationException.class);
        assertThatThrownBy(() -> DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(String.class, null, "test.missing-target", (source, context) -> 1))))
                .isInstanceOf(DataConverterConfigurationException.class);
    }

    @Test
    void exactConverterWinsAndNearestAssignableConverterIsSelected() {
        DataConversionService service = DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(Number.class, String.class, "test.number.string", (source, context) -> "number"),
                rule(Integer.class, String.class, "test.integer.string", (source, context) -> "integer")));

        assertThat(service.convert(1, String.class)).isEqualTo("integer");
        assertThat(service.convert(1L, String.class)).isEqualTo("number");
    }

    @Test
    void exactConvertersWinOverBuiltInCapabilities() {
        DataConversionService service = DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(String.class, String.class, "test.string.string", (source, context) -> "converted:" + source),
                rule(String[].class, List.class, "test.string-array.list", (source, context) -> List.of("custom")),
                rule(String.class, Status.class, "test.string.status", (source, context) -> Status.INACTIVE)));

        assertThat(service.convert("value", String.class)).isEqualTo("converted:value");
        List<?> convertedList = service.convert(new String[] { "value" }, List.class);
        assertThat(convertedList).isEqualTo(List.of("custom"));
        assertThat(service.convert("ACTIVE", Status.class)).isEqualTo(Status.INACTIVE);
    }

    @Test
    void rejectsEquallySpecificAssignableConvertersInBothLookupOperations() {
        DataConversionService service = DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(Left.class, String.class, "test.left.string", (source, context) -> "left"),
                rule(Right.class, String.class, "test.right.string", (source, context) -> "right")));

        assertThatThrownBy(() -> service.canConvert(Both.class, String.class))
                .isInstanceOf(DataConverterConfigurationException.class);
        assertThatThrownBy(() -> service.convert(new Both(), String.class))
                .isInstanceOf(DataConverterConfigurationException.class);
    }

    @Test
    void standardConvertersUseExplicitTemporalContext() {
        ConversionContext context = new ConversionContext(ZoneId.of("America/Sao_Paulo"), Locale.CANADA_FRENCH);
        DataConversionService service = DefaultDataConversionService.withConverters(context, StandardConverters.all());

        assertThat(service.convert(LocalDateTime.of(2024, 1, 2, 3, 4), ZonedDateTime.class))
                .isEqualTo(ZonedDateTime.of(2024, 1, 2, 3, 4, 0, 0, context.zoneId()));
        assertThat(service.convert(LocalDate.of(2024, 1, 2), ZonedDateTime.class))
                .isEqualTo(ZonedDateTime.of(2024, 1, 2, 0, 0, 0, 0, context.zoneId()));
        assertThat(service.convert("2024-01-02T03:04", ZonedDateTime.class))
                .isEqualTo(ZonedDateTime.of(2024, 1, 2, 3, 4, 0, 0, context.zoneId()));
    }

    @Test
    void nullSourcesAndFoldableCopiesFollowThePublicContract() {
        DataConversionService service = DefaultDataConversionService.standard();
        List<String> source = new ArrayList<>(List.of("one", "two"));
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("values", source);

        assertThat(service.convert(null, Integer.class)).isNull();
        assertThat((Object) service.copyFoldableValue(null)).isNull();

        List<String> copiedList = service.copyFoldableValue(source);
        Map<String, List<String>> copiedMap = service.copyFoldableValue(map);
        source.add("three");

        assertThat(copiedList).containsExactly("one", "two");
        assertThat(copiedMap).containsEntry("values", List.of("one", "two"));
        copiedList.add("four");
        assertThat(source).containsExactly("one", "two", "three");
        assertThatThrownBy(() -> service.copyFoldableValue(new StringBuilder("mutable")))
                .isInstanceOf(NonFoldableValueException.class);
        assertThatThrownBy(() -> service.copyFoldableValue(new ArrayList<>(Arrays.asList("one", null))))
                .isInstanceOf(NonFoldableValueException.class);
    }

    @Test
    void mutableValuesAreCopiedRecursivelyWhileIterationOrderIsPreserved() {
        DataConversionService service = DefaultDataConversionService.standard();
        Timestamp timestamp = Timestamp.valueOf("2024-01-02 03:04:05.123456789");
        LinkedHashSet<String> set = new LinkedHashSet<>(List.of("second", "first"));
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("timestamp", timestamp);
        values.put("set", set);

        Map<String, Object> copy = service.copyFoldableValue(values);
        Timestamp copiedTimestamp = (Timestamp) copy.get("timestamp");

        assertThat(copy.keySet()).containsExactly("timestamp", "set");
        assertThat(((java.util.Set<?>) copy.get("set")).toArray()).containsExactly("second", "first");
        assertThat(copiedTimestamp).isEqualTo(timestamp).isNotSameAs(timestamp);
        assertThat(copiedTimestamp.getNanos()).isEqualTo(123456789);
    }

    @Test
    void convertersMustReturnNonNullFoldableValues() {
        DataConversionService nullService = DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(String.class, Integer.class, "test.null-result", (source, context) -> null)));
        DataConversionService mutableService = DefaultDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(String.class, StringBuilder.class, "test.mutable-result", (source, context) -> new StringBuilder(source))));

        assertThat(mutableService.canConvert(String.class, StringBuilder.class)).isFalse();
        assertThatThrownBy(() -> nullService.convert("value", Integer.class))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> mutableService.convert("value", StringBuilder.class))
                .isInstanceOf(NonFoldableValueException.class);
    }

    @Test
    void standardCapabilitiesIncludeEnumsAndArrayConversions() {
        DataConversionService service = DefaultDataConversionService.standard();
        String[] source = { "1", null, "3" };

        assertThat(service.canConvert(String[].class, Integer[].class)).isTrue();
        assertThat(service.canConvert(String[][].class, Integer[][].class)).isTrue();
        assertThat(service.canConvert(int[].class, long[].class)).isTrue();
        assertThat(service.canConvert(int[].class, String[].class)).isTrue();
        assertThat(service.canConvert(String[].class, Thread[].class)).isFalse();
        assertThat(service.canConvert(Thread[].class, Thread[].class)).isFalse();
        assertThat(service.canConvert(ArrayList.class, List.class)).isTrue();
        assertThat(service.canConvert(ArrayList.class, ArrayList.class)).isFalse();
        assertThat(service.canConvert(AtomicInteger.class, AtomicInteger.class)).isFalse();
        assertThat(service.canConvert(List.class, Thread[].class)).isFalse();
        assertThat(service.convert("active", Status.class)).isEqualTo(Status.ACTIVE);
        assertThat(service.convert(1, Status.class)).isEqualTo(Status.INACTIVE);
        assertThat(service.convert(List.of("1", "2"), int[].class)).containsExactly(1, 2);
        assertThat(service.convert(source, Integer[].class)).containsExactly(1, null, 3);
        List<?> convertedList = service.convert(new String[] { "1", "3" }, List.class);
        assertThat(convertedList).isEqualTo(List.of("1", "3"));
        assertThatThrownBy(() -> service.convert(source, List.class))
                .isInstanceOf(NonFoldableValueException.class);
        assertThatThrownBy(() -> service.convert(new Thread[0], Thread[].class))
                .isInstanceOf(NonFoldableValueException.class);
        assertThatThrownBy(() -> service.convert(List.of(), Thread[].class))
                .isInstanceOf(NonFoldableValueException.class);
        assertThatThrownBy(() -> service.convert(new Thread[] { null }, Thread[].class))
                .isInstanceOf(NonFoldableValueException.class);
        assertThatThrownBy(() -> service.convert(new ArrayList<>(), ArrayList.class))
                .isInstanceOf(NoDataConverterFoundException.class);
    }

    @Test
    void arbitraryTemporalAccessorImplementationsAreNotFoldableByDefault() {
        DataConversionService service = DefaultDataConversionService.standard();
        TemporalAccessor mutableTemporal = new MutableTemporalAccessor();

        assertThat(service.canConvert(MutableTemporalAccessor.class, TemporalAccessor.class)).isFalse();
        assertThatThrownBy(() -> service.copyFoldableValue(mutableTemporal))
                .isInstanceOf(NonFoldableValueException.class);
    }

    private static String sha256(String value) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder(hash.length * 2);
        for (byte valueByte : hash) {
            result.append(String.format("%02x", valueByte));
        }
        return result.toString();
    }

    private static <S, T> DataConverter<S, T> rule(
            Class<S> sourceType,
            Class<T> targetType,
            String identity,
            BiFunction<S, ConversionContext, T> conversion) {
        return new DataConverter<>() {
            @Override
            public Class<S> sourceType() {
                return sourceType;
            }

            @Override
            public Class<T> targetType() {
                return targetType;
            }

            @Override
            public String ruleIdentity() {
                return identity;
            }

            @Override
            public T convert(S source, ConversionContext context) {
                return conversion.apply(source, context);
            }
        };
    }

    private interface Left {
    }

    private interface Right {
    }

    private static final class Both implements Left, Right {
    }

    private static final class MutableTemporalAccessor implements TemporalAccessor {
        @Override
        public boolean isSupported(TemporalField field) {
            return false;
        }

        @Override
        public long getLong(TemporalField field) {
            throw new UnsupportedOperationException("No temporal fields are supported");
        }
    }

    private enum Status {
        ACTIVE,
        INACTIVE
    }
}
