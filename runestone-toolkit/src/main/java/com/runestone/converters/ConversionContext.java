package com.runestone.converters;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;

public record ConversionContext(ZoneId zoneId, Locale locale) {

    public ConversionContext {
        Objects.requireNonNull(zoneId, "Zone ID must be provided");
        Objects.requireNonNull(locale, "Locale must be provided");
    }

    public static ConversionContext standard() {
        return new ConversionContext(ZoneId.of("UTC"), Locale.ROOT);
    }

    public static ConversionContext system() {
        return new ConversionContext(ZoneId.systemDefault(), Locale.getDefault());
    }
}
