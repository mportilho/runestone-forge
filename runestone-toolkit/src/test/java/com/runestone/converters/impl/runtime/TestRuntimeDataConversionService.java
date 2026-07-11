package com.runestone.converters.impl.runtime;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConverter;
import com.runestone.converters.DataConverterConfigurationException;
import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.converters.RuntimeDataConverter;
import com.runestone.converters.RuntimeStandardConverters;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestRuntimeDataConversionService {

    @Test
    void standardServiceUsesOperationalSystemContextAndRuntimeConverters() {
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.standard();

        assertThat(service.conversionContext()).isEqualTo(ConversionContext.system());
        assertThat(RuntimeDataConversionService.class.getMethods())
                .extracting(Method::getName)
                .doesNotContain("conversionProfileIdentity", "conversionProfileHash", "copyFoldableValue");
        assertThat(RuntimeStandardConverters.all())
                .isNotEmpty()
                .allSatisfy(converter -> assertThat(converter).isNotInstanceOf(DataConverter.class));
        assertThat(service.canConvert(String.class, Integer.class)).isTrue();
        assertThat(service.convert("123", Integer.class)).isEqualTo(123);
    }

    @Test
    void customRuntimeServiceUsesProvidedContextAndNearestConverter() {
        ConversionContext context = new ConversionContext(ZoneId.of("America/Sao_Paulo"), Locale.CANADA_FRENCH);
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.withConverters(context, List.of(
                rule(Number.class, String.class, (source, conversionContext) -> conversionContext.locale().toLanguageTag() + ":number"),
                rule(Integer.class, String.class, (source, conversionContext) -> conversionContext.locale().toLanguageTag() + ":integer")));

        assertThat(service.conversionContext()).isEqualTo(context);
        assertThat(service.convert(10, String.class)).isEqualTo("fr-CA:integer");
        assertThat(service.convert(10L, String.class)).isEqualTo("fr-CA:number");
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
        RuntimeDataConversionService service = DefaultRuntimeDataConversionService.withConverters(ConversionContext.standard(), List.of(
                rule(Left.class, String.class, (source, context) -> "left"),
                rule(Right.class, String.class, (source, context) -> "right")));

        assertThatThrownBy(() -> service.canConvert(Both.class, String.class))
                .isInstanceOf(DataConverterConfigurationException.class);
        assertThatThrownBy(() -> service.convert(new Both(), String.class))
                .isInstanceOf(DataConverterConfigurationException.class);
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
