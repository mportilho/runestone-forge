package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.DataConverter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestStringConverterContract {

    @Test
    public void testConverterMetadata() {
        List<DataConverter<?, ?>> converters = List.of(
                new StringToBigDecimalConverter(),
                new StringToBigIntegerConverter(),
                new StringToBooleanConverter(),
                new StringToByteArrayConverter(),
                new StringToByteConverter(),
                new StringToCharacterConverter(),
                new StringToCharsetConverter(),
                new StringToClassConverter(),
                new StringToCurrencyConverter(),
                new StringToDoubleConverter(),
                new StringToDurationConverter(),
                new StringToFileConverter(),
                new StringToFloatConverter(),
                new StringToInetAddressConverter(),
                new StringToInstantConverter(),
                new StringToIntegerConverter(),
                new StringToJavaSqlDateConverter(),
                new StringToJavaUtilDateConverter(),
                new StringToLocalDateConverter(),
                new StringToLocalDateTimeConverter(),
                new StringToLocalTimeConverter(),
                new StringToLocaleConverter(),
                new StringToLongConverter(),
                new StringToOffsetDateTimeConverter(),
                new StringToOffsetTimeConverter(),
                new StringToPathConverter(),
                new StringToPatternConverter(),
                new StringToPeriodConverter(),
                new StringToShortConverter(),
                new StringToTimestampConverter(),
                new StringToURIConverter(),
                new StringToURLConverter(),
                new StringToUUIDConverter(),
                new StringToYearConverter(),
                new StringToYearMonthConverter(),
                new StringToZoneIdConverter(),
                new StringToZoneOffsetConverter(),
                new StringToZonedDateTimeConverter()
        );

        Assertions.assertThat(converters)
                .allSatisfy(converter -> {
                    Assertions.assertThat(converter.sourceType()).isEqualTo(String.class);
                    Assertions.assertThat(converter.targetType()).isNotNull();
                    Assertions.assertThat(converter.ruleIdentity()).matches("[a-z0-9][a-z0-9._:-]*");
                });
        Assertions.assertThat(converters)
                .extracting(DataConverter::ruleIdentity)
                .doesNotHaveDuplicates();
    }
}
