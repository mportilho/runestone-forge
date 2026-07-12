package com.runestone.converters;

import com.runestone.converters.impl.runtime.dates.RuntimeTemporalConverters;
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
import com.runestone.converters.impl.runtime.strings.StringToByteArrayRuntimeConverter;
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

import java.util.List;
import java.util.stream.Stream;

public final class RuntimeStandardConverters {

    private static final List<RuntimeDataConverter<?, ?>> ALL = Stream.concat(
            RuntimeTemporalConverters.all().stream(),
            Stream.of(
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
            new StringToByteArrayRuntimeConverter(),
            new StringToByteRuntimeConverter(),
            new StringToCharacterRuntimeConverter(),
            new StringToCurrencyRuntimeConverter(),
            new StringToDoubleRuntimeConverter(),
            new StringToFloatRuntimeConverter(),
            new StringToIntegerRuntimeConverter(),
            new StringToLocaleRuntimeConverter(),
            new StringToLongRuntimeConverter(),
            new StringToPatternRuntimeConverter(),
            new StringToShortRuntimeConverter(),
            new StringToURIRuntimeConverter(),
            new StringToUUIDRuntimeConverter()))
            .toList();

    private RuntimeStandardConverters() {
    }

    public static List<RuntimeDataConverter<?, ?>> all() {
        return ALL;
    }
}
