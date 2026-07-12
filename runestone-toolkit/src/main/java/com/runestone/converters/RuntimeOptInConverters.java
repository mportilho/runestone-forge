package com.runestone.converters;

import com.runestone.converters.impl.runtime.strings.StringToCharsetRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToClassRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToFileRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToInetAddressRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToPathRuntimeConverter;
import com.runestone.converters.impl.runtime.strings.StringToURLRuntimeConverter;

import java.util.List;

public final class RuntimeOptInConverters {

    private static final List<RuntimeDataConverter<?, ?>> ALL = List.of(
            new StringToCharsetRuntimeConverter(),
            new StringToClassRuntimeConverter(),
            new StringToFileRuntimeConverter(),
            new StringToInetAddressRuntimeConverter(),
            new StringToPathRuntimeConverter(),
            new StringToURLRuntimeConverter());

    private RuntimeOptInConverters() {
    }

    public static List<RuntimeDataConverter<?, ?>> all() {
        return ALL;
    }
}
