package com.runestone.converters;

import com.runestone.converters.impl.dates.SqlDateToLocalDateConverter;
import com.runestone.converters.impl.dates.SqlDateToLocalDateTimeConverter;
import com.runestone.converters.impl.dates.SqlDateToLocalTimeConverter;
import com.runestone.converters.impl.dates.SqlDateToZonedDateTimeConverter;
import com.runestone.converters.impl.dates.StringToTemporalConverter;
import com.runestone.converters.impl.dates.TemporalToLocalDateConverter;
import com.runestone.converters.impl.dates.TemporalToLocalDateTimeConverter;
import com.runestone.converters.impl.dates.TemporalToLocalTimeConverter;
import com.runestone.converters.impl.dates.TemporalToZonedDateTimeConverter;
import com.runestone.converters.impl.dates.UtilDateToLocalDateConverter;
import com.runestone.converters.impl.dates.UtilDateToLocalDateTimeConverter;
import com.runestone.converters.impl.dates.UtilDateToLocalTimeConverter;
import com.runestone.converters.impl.dates.UtilDateToZonedDateTimeConverter;
import com.runestone.converters.impl.numbers.NumberToBigDecimalConverter;
import com.runestone.converters.impl.numbers.NumberToBigIntegerConverter;
import com.runestone.converters.impl.numbers.NumberToByteConverter;
import com.runestone.converters.impl.numbers.NumberToDoubleConverter;
import com.runestone.converters.impl.numbers.NumberToFloatConverter;
import com.runestone.converters.impl.numbers.NumberToIntConverter;
import com.runestone.converters.impl.numbers.NumberToLongConverter;
import com.runestone.converters.impl.numbers.NumberToShortConverter;
import com.runestone.converters.impl.numbers.NumberToStringConverter;
import com.runestone.converters.impl.strings.StringToBigDecimalConverter;
import com.runestone.converters.impl.strings.StringToBigIntegerConverter;
import com.runestone.converters.impl.strings.StringToBooleanConverter;
import com.runestone.converters.impl.strings.StringToByteArrayConverter;
import com.runestone.converters.impl.strings.StringToByteConverter;
import com.runestone.converters.impl.strings.StringToCharacterConverter;
import com.runestone.converters.impl.strings.StringToCurrencyConverter;
import com.runestone.converters.impl.strings.StringToDoubleConverter;
import com.runestone.converters.impl.strings.StringToDurationConverter;
import com.runestone.converters.impl.strings.StringToFloatConverter;
import com.runestone.converters.impl.strings.StringToInstantConverter;
import com.runestone.converters.impl.strings.StringToIntegerConverter;
import com.runestone.converters.impl.strings.StringToJavaSqlDateConverter;
import com.runestone.converters.impl.strings.StringToJavaUtilDateConverter;
import com.runestone.converters.impl.strings.StringToLocalDateConverter;
import com.runestone.converters.impl.strings.StringToLocalDateTimeConverter;
import com.runestone.converters.impl.strings.StringToLocalTimeConverter;
import com.runestone.converters.impl.strings.StringToLocaleConverter;
import com.runestone.converters.impl.strings.StringToLongConverter;
import com.runestone.converters.impl.strings.StringToOffsetDateTimeConverter;
import com.runestone.converters.impl.strings.StringToOffsetTimeConverter;
import com.runestone.converters.impl.strings.StringToPatternConverter;
import com.runestone.converters.impl.strings.StringToPeriodConverter;
import com.runestone.converters.impl.strings.StringToShortConverter;
import com.runestone.converters.impl.strings.StringToTimestampConverter;
import com.runestone.converters.impl.strings.StringToURIConverter;
import com.runestone.converters.impl.strings.StringToUUIDConverter;
import com.runestone.converters.impl.strings.StringToYearConverter;
import com.runestone.converters.impl.strings.StringToYearMonthConverter;
import com.runestone.converters.impl.strings.StringToZoneIdConverter;
import com.runestone.converters.impl.strings.StringToZoneOffsetConverter;
import com.runestone.converters.impl.strings.StringToZonedDateTimeConverter;

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
