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
import com.runestone.converters.impl.runtime.numbers.NumberToBigDecimalRuntimeConverter;
import com.runestone.converters.impl.runtime.numbers.NumberToBigIntegerRuntimeConverter;
import com.runestone.converters.impl.runtime.numbers.NumberToByteRuntimeConverter;
import com.runestone.converters.impl.runtime.numbers.NumberToDoubleRuntimeConverter;
import com.runestone.converters.impl.runtime.numbers.NumberToFloatRuntimeConverter;
import com.runestone.converters.impl.runtime.numbers.NumberToIntegerRuntimeConverter;
import com.runestone.converters.impl.runtime.numbers.NumberToLongRuntimeConverter;
import com.runestone.converters.impl.runtime.numbers.NumberToShortRuntimeConverter;
import com.runestone.converters.impl.runtime.numbers.NumberToStringRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToBigDecimalRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToBigIntegerRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToBooleanRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToByteRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToCharacterRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToCurrencyRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToDoubleRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToFloatRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToIntegerRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToLocaleRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToLongRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToPatternRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToShortRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToURIRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToUUIDRuntimeConverter;
import com.runestone.converters.impl.stable.strings.StringToByteArrayConverter;
import com.runestone.converters.impl.stable.strings.StringToDurationConverter;
import com.runestone.converters.impl.stable.strings.StringToInstantConverter;
import com.runestone.converters.impl.stable.strings.StringToJavaSqlDateConverter;
import com.runestone.converters.impl.stable.strings.StringToJavaUtilDateConverter;
import com.runestone.converters.impl.stable.strings.StringToLocalDateConverter;
import com.runestone.converters.impl.stable.strings.StringToLocalDateTimeConverter;
import com.runestone.converters.impl.stable.strings.StringToLocalTimeConverter;
import com.runestone.converters.impl.stable.strings.StringToOffsetDateTimeConverter;
import com.runestone.converters.impl.stable.strings.StringToOffsetTimeConverter;
import com.runestone.converters.impl.stable.strings.StringToPeriodConverter;
import com.runestone.converters.impl.stable.strings.StringToTimestampConverter;
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
            new NumberToBigDecimalRuntimeConverter(),
            new NumberToBigIntegerRuntimeConverter(),
            new NumberToByteRuntimeConverter(),
            new NumberToDoubleRuntimeConverter(),
            new NumberToFloatRuntimeConverter(),
            new NumberToIntegerRuntimeConverter(),
            new NumberToLongRuntimeConverter(),
            new NumberToShortRuntimeConverter(),
            new NumberToStringRuntimeConverter(),
            new StringToBigDecimalRuntimeConverter(),
            new StringToBigIntegerRuntimeConverter(),
            new StringToBooleanRuntimeConverter(),
            runtime(new StringToByteArrayConverter()),
            new StringToByteRuntimeConverter(),
            new StringToCharacterRuntimeConverter(),
            new StringToCurrencyRuntimeConverter(),
            new StringToDoubleRuntimeConverter(),
            runtime(new StringToDurationConverter()),
            new StringToFloatRuntimeConverter(),
            runtime(new StringToInstantConverter()),
            new StringToIntegerRuntimeConverter(),
            runtime(new StringToJavaSqlDateConverter()),
            runtime(new StringToJavaUtilDateConverter()),
            runtime(new StringToLocalDateConverter()),
            runtime(new StringToLocalDateTimeConverter()),
            runtime(new StringToLocalTimeConverter()),
            new StringToLocaleRuntimeConverter(),
            new StringToLongRuntimeConverter(),
            runtime(new StringToOffsetDateTimeConverter()),
            runtime(new StringToOffsetTimeConverter()),
            new StringToPatternRuntimeConverter(),
            runtime(new StringToPeriodConverter()),
            new StringToShortRuntimeConverter(),
            runtime(new StringToTimestampConverter()),
            new StringToURIRuntimeConverter(),
            new StringToUUIDRuntimeConverter(),
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
