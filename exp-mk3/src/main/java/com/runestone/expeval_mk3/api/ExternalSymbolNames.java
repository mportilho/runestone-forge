package com.runestone.expeval_mk3.api;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

final class ExternalSymbolNames {

    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
    private static final Set<String> RESERVED_CURRENT_TEMPORAL_NAMES = Set.of(
            "currDate",
            "currTime",
            "currDateTime");

    private ExternalSymbolNames() {
    }

    static String validate(String name) {
        Objects.requireNonNull(name, "name");
        if (!IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("external symbol name must be a valid identifier");
        }
        if (RESERVED_CURRENT_TEMPORAL_NAMES.contains(name)) {
            throw new IllegalArgumentException("external symbol name is reserved: " + name);
        }
        return name;
    }
}
