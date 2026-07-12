package com.runestone.converters.impl.runtime;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConverter;
import com.runestone.converters.DataConverterConfigurationException;
import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.converters.RuntimeDataConverter;
import com.runestone.converters.RuntimeStandardConverters;
import com.runestone.converters.impl.runtime.numbers.NumberToStringRuntimeConverter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestRuntimeDataConversionService {

    private static final String RUNTIME_IMPLEMENTATION_PACKAGE = "com.runestone.converters.impl.runtime";

    private static final List<Class<?>> NUMBER_TARGET_TYPES = List.of(
            BigDecimal.class,
            BigInteger.class,
            Byte.class,
            Double.class,
            Float.class,
            Integer.class,
            Long.class,
            Short.class,
            String.class);

    private static final List<Class<?>> SCALAR_STRING_TARGET_TYPES = List.of(
            BigDecimal.class,
            BigInteger.class,
            Boolean.class,
            Byte.class,
            Character.class,
            Currency.class,
            Double.class,
            Float.class,
            Integer.class,
            Locale.class,
            Long.class,
            Pattern.class,
            Short.class,
            URI.class,
            UUID.class);

    @Test
    void standardServiceUsesOperationalSystemContextAndRuntimeConverters() {
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.standard();

        assertThat(service.conversionContext().zoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(service.conversionContext().locale()).isEqualTo(Locale.getDefault());
        assertThat(RuntimeDataConversionService.class.getMethods())
                .extracting(Method::getName)
                .doesNotContain("conversionProfileIdentity", "conversionProfileHash", "copyFoldableValue");
        assertThat(RuntimeStandardConverters.all())
                .isNotEmpty()
                .allSatisfy(converter -> assertThat(converter).isNotInstanceOf(DataConverter.class));
        assertThat(RuntimeStandardConverters.all())
                .filteredOn(converter -> converter.sourceType() == Number.class && converter.targetType() == String.class)
                .singleElement()
                .isInstanceOf(NumberToStringRuntimeConverter.class);
        assertThat(service.canConvert(String.class, Integer.class)).isTrue();
        assertThat(service.convert("123", Integer.class)).isEqualTo(123);
        assertThat(service.convert(123, String.class)).isEqualTo("123");
    }

    @Test
    void numericAndScalarStringStandardRuntimeConvertersAreNativeRules() {
        NUMBER_TARGET_TYPES.forEach(targetType -> assertNativeRuntimeRule(Number.class, targetType));
        SCALAR_STRING_TARGET_TYPES.forEach(targetType -> assertNativeRuntimeRule(String.class, targetType));
    }

    @Test
    void standardRuntimeNumericAndScalarStringConversionsMatchExistingBehavior() {
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.standard();
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        assertThat(service.convert("123.45", BigDecimal.class)).isEqualByComparingTo("123.45");
        assertThat(service.convert("12345678901234567890", BigInteger.class))
                .isEqualTo(new BigInteger("12345678901234567890"));
        assertThat(service.convert(" yes ", Boolean.class)).isTrue();
        assertThat(service.convert("anything-else", Boolean.class)).isFalse();
        assertThat(service.convert("abc", Character.class)).isEqualTo('a');
        assertThat(service.convert("en_US", Locale.class)).isEqualTo(Locale.US);
        assertThat(service.convert("https://runestone.local/path", URI.class))
                .isEqualTo(URI.create("https://runestone.local/path"));
        assertThat(service.convert(uuid.toString(), UUID.class)).isEqualTo(uuid);

        assertThat(service.convert(new AtomicInteger(7), BigDecimal.class)).isEqualByComparingTo("7");
        assertThat(service.convert(new BigDecimal("12.9"), Long.class)).isEqualTo(12L);
        assertThat(service.convert(257, Byte.class)).isEqualTo((byte) 1);
    }

    @Test
    void customRuntimeServiceUsesProvidedContextAndNearestConverter() {
        ConversionContext context = new ConversionContext(ZoneId.of("America/Sao_Paulo"), Locale.CANADA_FRENCH);
        RuntimeDataConverter<Number, String> numberToString = rule(
                Number.class,
                String.class,
                (source, conversionContext) -> conversionContext.locale().toLanguageTag() + ":number");
        RuntimeDataConverter<Integer, String> integerToString = rule(
                Integer.class,
                String.class,
                (source, conversionContext) -> conversionContext.locale().toLanguageTag() + ":integer");
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.withConverters(context, List.of(
                numberToString,
                integerToString));
        RuntimeDataConversionService reversedService = DefaultRuntimeDataConversionService.withConverters(context, List.of(
                integerToString,
                numberToString));

        assertThat(service.conversionContext()).isEqualTo(context);
        assertThat(service.convert(10, String.class)).isEqualTo("fr-CA:integer");
        assertThat(service.convert(10L, String.class)).isEqualTo("fr-CA:number");
        assertThat(reversedService.convert(10, String.class)).isEqualTo("fr-CA:integer");
        assertThat(reversedService.convert(10L, String.class)).isEqualTo("fr-CA:number");
    }

    @Test
    void standardRuntimeConvertersUseExplicitTemporalContext() {
        ConversionContext context = new ConversionContext(ZoneId.of("America/Sao_Paulo"), Locale.CANADA_FRENCH);
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.withConverters(
                context,
                RuntimeStandardConverters.all());

        assertThat(service.convert(LocalDateTime.of(2024, 1, 2, 3, 4), ZonedDateTime.class))
                .isEqualTo(ZonedDateTime.of(2024, 1, 2, 3, 4, 0, 0, context.zoneId()));
        assertThat(service.convert(LocalDate.of(2024, 1, 2), ZonedDateTime.class))
                .isEqualTo(ZonedDateTime.of(2024, 1, 2, 0, 0, 0, 0, context.zoneId()));
        assertThat(service.convert("2024-01-02T03:04", ZonedDateTime.class))
                .isEqualTo(ZonedDateTime.of(2024, 1, 2, 3, 4, 0, 0, context.zoneId()));
    }

    @Test
    void runtimeIdentityConversionReturnsSameInstance() {
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.standard();
        StringBuilder source = new StringBuilder("mutable");

        assertThat(service.canConvert(StringBuilder.class, CharSequence.class)).isTrue();
        assertThat(service.convert(source, CharSequence.class)).isSameAs(source);
        assertThat(service.convert(source, StringBuilder.class)).isSameAs(source);
    }

    @Test
    void runtimeConvertersMustReturnNonNullResultsForNonNullSources() {
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(String.class, Integer.class, (source, context) -> null)));

        assertThat(service.convert(null, Integer.class)).isNull();
        assertThatThrownBy(() -> service.convert("value", Integer.class))
                .isInstanceOf(DataConverterConfigurationException.class);
    }

    @Test
    void standardRuntimeCapabilitiesIncludeEnumsAndArrayConversions() {
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.standard();

        assertThat(service.canConvert(String[].class, Integer[].class)).isTrue();
        assertThat(service.convert("active", Status.class)).isEqualTo(Status.ACTIVE);
        assertThat(service.convert(1, Status.class)).isEqualTo(Status.INACTIVE);
        int[] primitiveArray = service.convert(List.of("1", "2"), int[].class);
        Integer[] boxedArray = service.convert(new String[] { "1", null, "3" }, Integer[].class);
        List<?> list = service.convert(new String[] { "1", "3" }, List.class);

        assertThat(primitiveArray).containsExactly(1, 2);
        assertThat(boxedArray).containsExactly(1, null, 3);
        assertThat(list).isEqualTo(List.of("1", "3"));
    }

    @Test
    void ambiguousRuntimeConvertersFailLookupAndConversion() {
        RuntimeDataConverter<Left, String> leftRule = rule(Left.class, String.class, (source, context) -> "left");
        RuntimeDataConverter<Right, String> rightRule = rule(Right.class, String.class, (source, context) -> "right");
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.withConverters(
                ConversionContext.standard(),
                List.of(leftRule, rightRule));
        RuntimeDataConversionService reversedService = DefaultRuntimeDataConversionService.withConverters(
                ConversionContext.standard(),
                List.of(rightRule, leftRule));
        String expectedMessage = "Ambiguous runtime converters for " + Both.class.getName() + " -> java.lang.String";

        assertThatThrownBy(() -> service.canConvert(Both.class, String.class))
                .isInstanceOf(DataConverterConfigurationException.class)
                .hasMessageContaining(expectedMessage);
        assertThatThrownBy(() -> service.convert(new Both(), String.class))
                .isInstanceOf(DataConverterConfigurationException.class)
                .hasMessageContaining(expectedMessage);
        assertThatThrownBy(() -> reversedService.canConvert(Both.class, String.class))
                .isInstanceOf(DataConverterConfigurationException.class)
                .hasMessageContaining(expectedMessage);
        assertThatThrownBy(() -> reversedService.convert(new Both(), String.class))
                .isInstanceOf(DataConverterConfigurationException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void duplicateRuntimeConvertersFailAtServiceCreation() {
        RuntimeDataConverter<String, Integer> first = rule(String.class, Integer.class, (source, context) -> 1);
        RuntimeDataConverter<String, Integer> second = rule(String.class, Integer.class, (source, context) -> 2);

        assertThatThrownBy(() -> DefaultRuntimeDataConversionService.withConverters(ConversionContext.standard(), List.of(first, second)))
                .isInstanceOf(DataConverterConfigurationException.class);
    }

    private static <S, T> RuntimeDataConverter<S, T> rule(
            Class<S> sourceType,
            Class<T> targetType,
            BiFunction<S, ConversionContext, T> conversion) {
        return new RuntimeDataConverter<>() {
            @Override
            public Class<S> sourceType() {
                return sourceType;
            }

            @Override
            public Class<T> targetType() {
                return targetType;
            }

            @Override
            public T convert(S source, ConversionContext context) {
                return conversion.apply(source, context);
            }
        };
    }

    private static void assertNativeRuntimeRule(Class<?> sourceType, Class<?> targetType) {
        RuntimeDataConverter<?, ?> converter = runtimeConverter(sourceType, targetType);

        assertThat(converter).isNotInstanceOf(DataConverter.class);
        assertThat(converter.getClass().getPackageName()).startsWith(RUNTIME_IMPLEMENTATION_PACKAGE);
        assertThat(converter.getClass().getMethods())
                .extracting(Method::getName)
                .doesNotContain("ruleIdentity");
    }

    private static RuntimeDataConverter<?, ?> runtimeConverter(Class<?> sourceType, Class<?> targetType) {
        return RuntimeStandardConverters.all().stream()
                .filter(converter -> converter.sourceType() == sourceType && converter.targetType() == targetType)
                .reduce((first, second) -> {
                    throw new AssertionError("Duplicate runtime converter for "
                            + sourceType.getName() + " -> " + targetType.getName());
                })
                .orElseThrow(() -> new AssertionError("Missing runtime converter for "
                        + sourceType.getName() + " -> " + targetType.getName()));
    }

    private interface Left {
    }

    private interface Right {
    }

    private static final class Both implements Left, Right {
    }

    private enum Status {
        ACTIVE,
        INACTIVE
    }
}
