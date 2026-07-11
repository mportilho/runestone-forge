package com.runestone.converters;

import com.runestone.converters.impl.stable.dates.SqlDateToLocalDateConverter;
import com.runestone.converters.impl.stable.dates.SqlDateToLocalDateTimeConverter;
import com.runestone.converters.impl.stable.dates.SqlDateToLocalTimeConverter;
import com.runestone.converters.impl.stable.dates.SqlDateToZonedDateTimeConverter;
import com.runestone.converters.impl.stable.dates.StringToTemporalConverter;
import com.runestone.converters.impl.stable.dates.TemporalToLocalDateConverter;
import com.runestone.converters.impl.stable.dates.TemporalToLocalDateTimeConverter;
import com.runestone.converters.impl.stable.dates.TemporalToLocalTimeConverter;
import com.runestone.converters.impl.stable.dates.TemporalToZonedDateTimeConverter;
import com.runestone.converters.impl.stable.dates.UtilDateToLocalDateConverter;
import com.runestone.converters.impl.stable.dates.UtilDateToLocalDateTimeConverter;
import com.runestone.converters.impl.stable.dates.UtilDateToLocalTimeConverter;
import com.runestone.converters.impl.stable.dates.UtilDateToZonedDateTimeConverter;
import com.runestone.converters.impl.stable.numbers.NumberToBigDecimalConverter;
import com.runestone.converters.impl.stable.numbers.NumberToBigIntegerConverter;
import com.runestone.converters.impl.stable.numbers.NumberToByteConverter;
import com.runestone.converters.impl.stable.numbers.NumberToDoubleConverter;
import com.runestone.converters.impl.stable.numbers.NumberToFloatConverter;
import com.runestone.converters.impl.stable.numbers.NumberToIntConverter;
import com.runestone.converters.impl.stable.numbers.NumberToLongConverter;
import com.runestone.converters.impl.stable.numbers.NumberToShortConverter;
import com.runestone.converters.impl.stable.numbers.NumberToStringConverter;
import com.runestone.converters.impl.stable.strings.StringToBigDecimalConverter;
import com.runestone.converters.impl.stable.strings.StringToBigIntegerConverter;
import com.runestone.converters.impl.stable.strings.StringToBooleanConverter;
import com.runestone.converters.impl.stable.strings.StringToByteArrayConverter;
import com.runestone.converters.impl.stable.strings.StringToByteConverter;
import com.runestone.converters.impl.stable.strings.StringToCharacterConverter;
import com.runestone.converters.impl.stable.strings.StringToCurrencyConverter;
import com.runestone.converters.impl.stable.strings.StringToDoubleConverter;
import com.runestone.converters.impl.stable.strings.StringToDurationConverter;
import com.runestone.converters.impl.stable.strings.StringToFloatConverter;
import com.runestone.converters.impl.stable.strings.StringToInstantConverter;
import com.runestone.converters.impl.stable.strings.StringToIntegerConverter;
import com.runestone.converters.impl.stable.strings.StringToJavaSqlDateConverter;
import com.runestone.converters.impl.stable.strings.StringToJavaUtilDateConverter;
import com.runestone.converters.impl.stable.strings.StringToLocalDateConverter;
import com.runestone.converters.impl.stable.strings.StringToLocalDateTimeConverter;
import com.runestone.converters.impl.stable.strings.StringToLocalTimeConverter;
import com.runestone.converters.impl.stable.strings.StringToLocaleConverter;
import com.runestone.converters.impl.stable.strings.StringToLongConverter;
import com.runestone.converters.impl.stable.strings.StringToOffsetDateTimeConverter;
import com.runestone.converters.impl.stable.strings.StringToOffsetTimeConverter;
import com.runestone.converters.impl.stable.strings.StringToPatternConverter;
import com.runestone.converters.impl.stable.strings.StringToPeriodConverter;
import com.runestone.converters.impl.stable.strings.StringToShortConverter;
import com.runestone.converters.impl.stable.strings.StringToTimestampConverter;
import com.runestone.converters.impl.stable.strings.StringToURIConverter;
import com.runestone.converters.impl.stable.strings.StringToUUIDConverter;
import com.runestone.converters.impl.stable.strings.StringToYearConverter;
import com.runestone.converters.impl.stable.strings.StringToYearMonthConverter;
import com.runestone.converters.impl.stable.strings.StringToZoneIdConverter;
import com.runestone.converters.impl.stable.strings.StringToZoneOffsetConverter;
import com.runestone.converters.impl.stable.strings.StringToZonedDateTimeConverter;

import java.util.List;

public final class RuntimeStandardConverters {

    private static final List<RuntimeDataConverter<?, ?>> ALL = List.of(
            runtime(new SqlDateToLocalDateConverter()),
            runtime(new SqlDateToLocalDateTimeConverter()),
            runtime(new SqlDateToLocalTimeConverter()),
            runtime(new SqlDateToZonedDateTimeConverter()),
            runtime(new StringToTemporalConverter()),
            runtime(new TemporalToLocalDateConverter()),
            runtime(new TemporalToLocalDateTimeConverter()),
            runtime(new TemporalToLocalTimeConverter()),
            runtime(new TemporalToZonedDateTimeConverter()),
            runtime(new UtilDateToLocalDateConverter()),
            runtime(new UtilDateToLocalDateTimeConverter()),
            runtime(new UtilDateToLocalTimeConverter()),
            runtime(new UtilDateToZonedDateTimeConverter()),
            runtime(new NumberToBigDecimalConverter()),
            runtime(new NumberToBigIntegerConverter()),
            runtime(new NumberToByteConverter()),
            runtime(new NumberToDoubleConverter()),
            runtime(new NumberToFloatConverter()),
            runtime(new NumberToIntConverter()),
            runtime(new NumberToLongConverter()),
            runtime(new NumberToShortConverter()),
            runtime(new NumberToStringConverter()),
            runtime(new StringToBigDecimalConverter()),
            runtime(new StringToBigIntegerConverter()),
            runtime(new StringToBooleanConverter()),
            runtime(new StringToByteArrayConverter()),
            runtime(new StringToByteConverter()),
            runtime(new StringToCharacterConverter()),
            runtime(new StringToCurrencyConverter()),
            runtime(new StringToDoubleConverter()),
            runtime(new StringToDurationConverter()),
            runtime(new StringToFloatConverter()),
            runtime(new StringToInstantConverter()),
            runtime(new StringToIntegerConverter()),
            runtime(new StringToJavaSqlDateConverter()),
            runtime(new StringToJavaUtilDateConverter()),
            runtime(new StringToLocalDateConverter()),
            runtime(new StringToLocalDateTimeConverter()),
            runtime(new StringToLocalTimeConverter()),
            runtime(new StringToLocaleConverter()),
            runtime(new StringToLongConverter()),
            runtime(new StringToOffsetDateTimeConverter()),
            runtime(new StringToOffsetTimeConverter()),
            runtime(new StringToPatternConverter()),
            runtime(new StringToPeriodConverter()),
            runtime(new StringToShortConverter()),
            runtime(new StringToTimestampConverter()),
            runtime(new StringToURIConverter()),
            runtime(new StringToUUIDConverter()),
            runtime(new StringToYearConverter()),
            runtime(new StringToYearMonthConverter()),
            runtime(new StringToZoneIdConverter()),
            runtime(new StringToZoneOffsetConverter()),
            runtime(new StringToZonedDateTimeConverter()));

    private RuntimeStandardConverters() {
    }

    public static List<RuntimeDataConverter<?, ?>> all() {
        return ALL;
    }

    private static <S, T> RuntimeDataConverter<S, T> runtime(DataConverter<S, T> converter) {
        return new RuntimeStandardDataConverter<>(converter);
    }

    private record RuntimeStandardDataConverter<S, T>(DataConverter<S, T> delegate) implements RuntimeDataConverter<S, T> {

        @Override
        public Class<S> sourceType() {
            return delegate.sourceType();
        }

        @Override
        public Class<T> targetType() {
            return delegate.targetType();
        }

        @Override
        public T convert(S source, ConversionContext context) {
            return delegate.convert(source, context);
        }
    }
}
