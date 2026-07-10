package com.runestone.expeval_mk3.api;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Reserved dynamic simple-name values known to an Ambiente de Expressao.
 */
public enum CurrentTemporalValue {
    DATE("currDate", ScalarType.DATE),
    TIME("currTime", ScalarType.TIME),
    DATETIME("currDateTime", ScalarType.DATETIME);

    private final String simpleName;
    private final ScalarType expressionType;

    CurrentTemporalValue(String simpleName, ScalarType expressionType) {
        this.simpleName = Objects.requireNonNull(simpleName, "simpleName");
        this.expressionType = Objects.requireNonNull(expressionType, "expressionType");
    }

    public String simpleName() {
        return simpleName;
    }

    public ScalarType expressionType() {
        return expressionType;
    }

    public static Optional<CurrentTemporalValue> findBySimpleName(String simpleName) {
        Objects.requireNonNull(simpleName, "simpleName");
        return Arrays.stream(values())
                .filter(value -> value.simpleName.equals(simpleName))
                .findFirst();
    }

    static boolean isReservedSimpleName(String simpleName) {
        return findBySimpleName(simpleName).isPresent();
    }
}
