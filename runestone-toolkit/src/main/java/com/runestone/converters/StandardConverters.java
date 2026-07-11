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

public final class StandardConverters {

    private static final List<DataConverter<?, ?>> ALL = List.of(
            new SqlDateToLocalDateConverter(),
            new SqlDateToLocalDateTimeConverter(),
            new SqlDateToLocalTimeConverter(),
            new SqlDateToZonedDateTimeConverter(),
            new StringToTemporalConverter(),
            new TemporalToLocalDateConverter(),
            new TemporalToLocalDateTimeConverter(),
            new TemporalToLocalTimeConverter(),
            new TemporalToZonedDateTimeConverter(),
            new UtilDateToLocalDateConverter(),
            new UtilDateToLocalDateTimeConverter(),
            new UtilDateToLocalTimeConverter(),
            new UtilDateToZonedDateTimeConverter(),
            new NumberToBigDecimalConverter(),
            new NumberToBigIntegerConverter(),
            new NumberToByteConverter(),
            new NumberToDoubleConverter(),
            new NumberToFloatConverter(),
            new NumberToIntConverter(),
            new NumberToLongConverter(),
            new NumberToShortConverter(),
            new NumberToStringConverter(),
            new StringToBigDecimalConverter(),
            new StringToBigIntegerConverter(),
            new StringToBooleanConverter(),
            new StringToByteArrayConverter(),
            new StringToByteConverter(),
            new StringToCharacterConverter(),
            new StringToCurrencyConverter(),
            new StringToDoubleConverter(),
            new StringToDurationConverter(),
            new StringToFloatConverter(),
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
            new StringToPatternConverter(),
            new StringToPeriodConverter(),
            new StringToShortConverter(),
            new StringToTimestampConverter(),
            new StringToURIConverter(),
            new StringToUUIDConverter(),
            new StringToYearConverter(),
            new StringToYearMonthConverter(),
            new StringToZoneIdConverter(),
            new StringToZoneOffsetConverter(),
            new StringToZonedDateTimeConverter());

    private StandardConverters() {
    }

    public static List<DataConverter<?, ?>> all() {
        return ALL;
    }
}
