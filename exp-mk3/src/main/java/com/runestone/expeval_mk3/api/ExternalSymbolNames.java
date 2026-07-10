package com.runestone.expeval_mk3.api;

import java.util.Objects;
import java.util.regex.Pattern;

final class ExternalSymbolNames {

    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");

    private ExternalSymbolNames() {
    }

    static String validate(String name) {
        Objects.requireNonNull(name, "name");
        if (!IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("external symbol name must be a valid identifier");
        }
        if (CurrentTemporalValue.isReservedSimpleName(name)) {
            throw new IllegalArgumentException("external symbol name is reserved: " + name);
        }
        return name;
    }
}
